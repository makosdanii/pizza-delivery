<template>
  <DataTable
      name="Car"
      :headers="headers"
      :items="cars"
      :schema="schema"
      :cannot-create="role !== 'admin'"
      :cannot-delete="role !== 'admin'"
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
            <v-text-field name="license" v-model="editedItem.license" label="License" :disabled="role !== 'admin'"
                          :error-messages="errors.license"/>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-select
                :disabled="role !== 'admin'"
                :items="drivers"
                item-title="name"
                item-value="id"
                label="Select"
                name="userByUserId"
                v-model="editedItem.userByUserId.id"
                :error-messages="errors.userByUserId"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-btn prepend-icon="mdi-login-variant"
                   :disabled="role === 'admin' || editedItem.userByUserId.id.length !== 0"
                   @click="drive">Drive
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-btn prepend-icon="mdi-logout-variant" @click="leave" :disabled="editedItem.userByUserId.id.length === 0">
              Leave
            </v-btn>
          </v-col>
        </v-row>
      </v-container>
    </template>
    <template #error><b>Cars</b> should be unique by <b>license</b>. <br/> When deleting please make sure that it is
      neither
      referenced in any <b>delivery records</b> nor <b>inventory record</b>. <i>Simulation</i> restart might be
      necessary.
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
  license: "",
  userByUserId: {
    id: "",
  },
}
export default {
  name: "CarPage",
  components: {
    DataTable,
  },

  inject: ['cars', 'users', 'listUsers', 'listCars', 'role'],

  methods: {
    drive() {
      this.editedItem.userByUserId.id = server.id()
      server.updateCar(this.editedItem)
          .then((promise) => {
            if (promise.status === 201) {
              this.listCars()
              this.$refs.table.snackText = "Updated"
              this.$refs.table.color = "green"
              this.$refs.table.snack = true
            }
          }).catch(err => {
        if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
          this.$refs.table.snackText = "Operation denied"
          this.$refs.table.color = "red"
          this.$refs.table.snack = true
        }
      })
      this.reset()
    },
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            if (!this.editedItem.userByUserId.id) delete this.editedItem.userByUserId
            !this.editedItem.id ?
                server.addCar(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listCars()
                        this.$refs.table.snackText = "Created"
                        this.$refs.table.color = "green"
                        this.$refs.table.snack = true
                      }
                    }).catch(err => {
                  if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
                    this.$refs.table.snackText = "Operation denied"
                    this.$refs.table.color = "red"
                    this.$refs.table.snack = true
                  }
                })
                :
                server.updateCar(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listCars()
                        this.$refs.table.snackText = "Updated"
                        this.$refs.table.color = "green"
                        this.$refs.table.snack = true
                      }
                    }).catch(err => {
                  if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
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
      this.editedItem = _.cloneDeep(this.cars.filter(car => car.id === item.id)[0])
      if (!this.editedItem.userByUserId)
        this.editedItem = {...this.editedItem, userByUserId: {id: ''}}
      this.$refs.table.dialog = true
    },
    erase(item) {
      if (confirm(`This car will be deleted`)) {
        server.deleteCar(item.id).then((promise) => {
          if (promise.status === 200) {
            this.listCars()
          }
          this.$refs.table.snackText = "Deleted"
          this.$refs.table.color = "green"
          this.$refs.table.snack = true
        }).catch(err => {
          if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
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
    },
    leave() {
      delete this.editedItem.userByUserId
      server.updateCar(this.editedItem)
          .then((promise) => {
            if (promise.status === 201) {
              this.listCars()
              this.$refs.table.snackText = "Updated"
              this.$refs.table.color = "green"
              this.$refs.table.snack = true
            }
          }).catch(err => {
        if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
          this.$refs.table.snackText = "Operation denied"
          this.$refs.table.color = "red"
          this.$refs.table.snack = true
        }
      })
      this.reset()
    }
  },

  data() {
    return {
      headers: [
        {title: "License", key: "license"},
        {title: "Driver", key: "userByUserId.name"},
        {title: "Actions", key: "actions", sortable: false},
      ],
      editedItem: _.cloneDeep(defaultItem),
      schema: yup.object({
        license: yup.string().required(),
      }),
      drivers: [],
      errors: {}
    };
  },

  async mounted() {
    await this.listCars()
    if (this.role !== 'admin') {
      return
    }
    await this.listUsers()
    this.drivers = this.users.filter(user => user.roleByRoleId.name === 'driver')
  },
};
</script>
