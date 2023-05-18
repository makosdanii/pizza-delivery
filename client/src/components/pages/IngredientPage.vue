<template>
  <DataTable
      name="Ingredient"
      :headers="headers"
      :items="ingredients"
      :schema="schema"
      @save="save"
      @delete="erase"
      @edit="edit"
      @cancel="reset"
      ref="table"
  >
    <template #card-container>
      <v-container>
        <v-row>
          <v-col cols="12" sm="6" md="4">
            <v-text-field name="name" v-model="editedItem.name" label="Name" :error-messages="errors.name"/>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field type="number" name="price" v-model="editedItem.price" label="Price"
                          :error-messages="errors.price"/>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-select
                :items="allergies"
                item-title="name"
                item-value="id"
                label="Select"
                name="allergyByAllergyId"
                v-model="editedItem.allergyByAllergyId.id"
                :error-messages="errors.allergyByAllergyId"
            ></v-select>
          </v-col>
        </v-row>
      </v-container>
    </template>
  </DataTable>
</template>

<script>
import server from "../../business/PizzaServerAPI.js";
import DataTable from "../DataTable.vue";
import * as yup from "yup";
import _ from "lodash";

const defaultItem = {
  id: 0,
  name: "",
  price: 0,
  allergyByAllergyId: {
    id: "",
  },
}
export default {
  name: "IngredientPage",
  components: {
    DataTable,
  },

  inject: ['ingredients', 'allergies', 'listAllergies', 'listIngredients'],

  methods: {
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            if (!this.editedItem.allergyByAllergyId.id) delete this.editedItem.allergyByAllergyId
            !this.editedItem.id ?
                server.addIngredient(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listIngredients()
                        this.$refs.table.snackText = "Created"
                        this.$refs.table.color = "green"
                        this.$refs.table.snack = true
                      }
                    }).catch(err => {
                  if (err.response.status === 400 || err.response.status === 401) {
                    this.$refs.table.snackText = "Operation denied"
                    this.$refs.table.color = "red"
                    this.$refs.table.snack = true
                  }
                })
                :
                server.updateIngredient(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listIngredients()
                        this.$refs.table.snackText = "Updated"
                        this.$refs.table.color = "green"
                        this.$refs.table.snack = true
                      }
                    }).catch(err => {
                  if (err.response.status === 400 || err.response.status === 401) {
                    this.$refs.table.snackText = "Operation denied"
                    this.$refs.table.color = "red"
                    this.$refs.table.snack = true
                  }
                })

            this.reset()
          })
          .catch((err) => {
            err.inner.forEach((error) => {
              this.errors = {...this.errors, [error.path]: error.message};
            });
          });
    },
    edit(item) {
      this.editedItem = _.cloneDeep(this.ingredients.filter(ingredient => ingredient.id === item.id)[0])
      if (!this.editedItem.allergyByAllergyId)
        this.editedItem = {...this.editedItem, allergyByAllergyId: {id: ""}}
      this.$refs.table.dialog = true
    },
    erase(item) {
      if (confirm(`This ingredient will be deleted`)) {
        server.deleteIngredient(item.id).then((promise) => {
          if (promise.status === 200) {
            this.listIngredients()
          }
          this.$refs.table.snackText = "Deleted"
          this.$refs.table.color = "green"
          this.$refs.table.snack = true
        }).catch(err => {
          if (err.response.status === 400 || err.response.status === 401) {
            this.$refs.table.snackText = "Operation denied"
            this.$refs.table.color = "red"
            this.$refs.table.snack = true
          }
        });
      }
    },
    reset() {
      this.editedItem = _.cloneDeep(defaultItem)
      this.$refs.table.dialog = false;
    }
  },

  data() {
    return {
      headers: [
        {title: "Name", key: "name"},
        {title: "Allergy", key: "allergyByAllergyId.name"},
        {title: "Price/Unit (Ft)", key: "price"},
        {title: "Actions", key: "actions", sortable: false},
      ],
      editedItem: _.cloneDeep(defaultItem),
      schema: yup.object({
        name: yup.string().required(),
        price: yup.number().min(1).required()
      }),
      errors: {}
    };
  },

  async mounted() {
    await this.listIngredients()
    await this.listAllergies()
  },
};
</script>
