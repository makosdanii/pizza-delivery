<template>
  <v-dialog v-model="dialog" @click:outside="cancel">
    <form @submit.prevent="add">
      <v-text-field name="quantity" v-model="editedItem.quantity" single-line hide-details
                    :error-messages="errors.quantity"/>
      <v-btn type="submit">
        Add quantity
      </v-btn>
    </form>
  </v-dialog>
  <tr>
    <td colspan="5">
      <v-container>
        <v-row>
          <v-col cols="auto">
            <v-select
                v-if="role === 'admin' || role === 'chef'"
                v-model="selected"
                :items="computeIngredients"
                label="Select Item"
                multiple
                chips
            >
              <template v-slot:item="{ item }">
                <v-checkbox v-model="selected" :value="item.raw.name" @click="select"
                            :label="label(item.raw.name)"/>
              </template>
            </v-select>
          </v-col>
          <v-col cols="auto">
            <v-chip
                v-for="chip in chips"
                :text="chip"
            />
          </v-col>
        </v-row>
      </v-container>
    </td>
  </tr>
  <v-snackbar v-model="snack" :timeout="2000">{{ snackText }}</v-snackbar>
</template>

<script>
import _ from "lodash";
import * as yup from "yup";
import server from "@/business/PizzaServerAPI.js";

const defaultItem = {
  ingredientByIngredientId: {
    id: 0
  },
  quantity: 0,
}
export default {
  name: "MenuExpandItem",
  inject: ['ingredients', 'listIngredients', 'role'],
  emits: ['updated'],
  props: {
    menuIngredientsById: Array,
    id: Number,
    width: Number
  },
  computed: {
    chips() {
      return _.uniq(this.menuIngredients
          .map(ingredient => ingredient.ingredientByIngredientId.allergyByAllergyId?.name))
          .filter(allergy => allergy)
    },
    computeIngredients() {
      return this.ingredients;
    }
  },
  methods: {
    label(name) {
      if (this.selected.includes(name))
        name = name.concat(` - ${this.selectedQuantity(name)} dkg`)
      return name
    },
    selectedQuantity(id) {
      const selectedIngredient = this.menuIngredients
          .filter(ingredient => id === ingredient.ingredientByIngredientId.name)
      return selectedIngredient.length ? selectedIngredient[0].quantity : ''
    },
    select(event) {
      const id = this.ingredients.filter(ingredient => ingredient.name === event.target.value)[0].id
      //check value to see if checkbox was checked or will be checked
      if (!this.selected.includes(event.target.value)) {
        this.editedItem.ingredientByIngredientId.id = id
        this.dialog = true;
      } else {
        server.unAssignIngredient(this.id, id)
            .then(promise => {
              if (promise.status === 200) {
                this.menuIngredients = promise.data.menuIngredientsById
                this.snackText = "Unassigned"
                this.snack = true
              }
            }).catch(err => {
          if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
            this.$refs.table.snackText = "Operation denied"
            this.$refs.table.color = "red"
            this.$refs.table.snack = true
          }
        })
      }
    },
    cancel() {
      const name = this.ingredients.filter(ingredient => ingredient.id === this.editedItem.ingredientByIngredientId.id)[0].name
      this.selected = this.selected.filter(selected => selected !== name)
      this.editedItem = _.cloneDeep(defaultItem)
    },
    add() {
      this.error = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            server.assignIngredient(this.id, this.editedItem)
                .then(promise => {
                  if (promise.status === 201) {
                    //just so template parts get re-evaluated
                    this.menuIngredients = _.cloneDeep(promise.data.menuIngredientsById)
                    this.selected = _.clone(this.selected)

                    this.editedItem = _.cloneDeep(defaultItem)
                    this.dialog = false
                    this.snackText = "Assigned"
                    this.snack = true
                  }
                }).catch(err => {
              if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
                this.$refs.table.snackText = "Operation denied"
                this.$refs.table.color = "red"
                this.$refs.table.snack = true
              }
            })
          })
          .catch((err) => {
            err.inner.forEach((error) => {
              this.errors = {...this.errors, [error.path]: error.message};
            });
          })
    }
  },

  data() {
    return {
      menuIngredients: this.menuIngredientsById,
      selected: [],
      dialog: false,
      snack: false,
      snackText: "",
      schema: yup.object({
        ingredientByIngredientId: yup.object().required(),
        quantity: yup.number().required().min(1)
      }),
      editedItem: _.cloneDeep(defaultItem),
      errors: {}
    }
  },
  async mounted() {
    this.selected = this.menuIngredients.map(ingredient => ingredient.ingredientByIngredientId.name)
    if (!this.ingredients.length && this.role !== 'customer')
      await this.listIngredients()
  },

  unmounted() {
    this.$emit('updated')
  }
}
</script>

<style scoped>

</style>