<template>
  <DataTable
      name="Allergy"
      :headers="headers"
      :items="allergies"
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
  name: ""
}
export default {
  name: "AllergyPage",
  components: {
    DataTable,
  },

  inject: ['allergies', 'listAllergies'],

  methods: {
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            !this.editedItem.id ?
                server.addAllergy(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listAllergies()
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
                server.updateAllergy(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listAllergies()
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
      this.editedItem = _.cloneDeep(this.allergies.filter(allergy => allergy.id === item.id)[0])
      this.$refs.table.dialog = true
    },
    erase(item) {
      if (confirm(`This allergy will be deleted`)) {
        server.deleteAllergy(item.id).then((promise) => {
          if (promise.status === 200) {
            this.listAllergies()
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
        {title: "Actions", key: "actions", sortable: false},
      ],
      editedItem: _.cloneDeep(defaultItem),
      schema: yup.object({
        name: yup.string().required(),
      }),
      errors: {}
    };
  },

  async mounted() {
    await this.listAllergies()
  },
};
</script>
