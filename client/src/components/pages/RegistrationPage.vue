<template>
  <v-card
      style="min-width: 500px">
    <v-container>
      <v-row>
        <v-col cols="12" sm="6" md="4">
          <v-text-field name="email" v-model="editedItem.email" label="Email*" :error-messages="errors.email"/>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-text-field name="name" v-model="editedItem.name" label="Name*"
                        :error-messages="errors.name"/>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-text-field
              name="password"
              type="password"
              v-model="editedItem.password"
              label="Password*"
              :error-messages="errors.password"/>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" sm="6" md="4">
          <v-autocomplete
              clearable
              label="Street"
              name="streetNameByStreetNameId"
              :items="streets"
              item-title="that"
              item-value="id"
              v-model="editedItem.streetNameByStreetNameId.id"
              :error-messages="errors.streetNameByStreetNameId"
          ></v-autocomplete>
        </v-col>
        <v-col cols="12" sm="6" md="4">
          <v-text-field style="max-width: 200px"
                        label="House Number" name="houseNo" type="number"
                        v-model="editedItem.houseNo" :error-messages="errors.houseNo"/>
        </v-col>
      </v-row>
      <v-col cols="12" sm="6" md="4">
        <v-btn @click="save">Save</v-btn>
      </v-col>
      <v-col cols="12" sm="6" md="4">
        <v-btn color="red" v-if="role.length" @click="erase" :disabled="role === 'admin'">Delete</v-btn>
      </v-col>
    </v-container>
    <v-list v-if="role === 'customer'">
      <v-divider horizontal/>
      <v-list-subheader>Order history</v-list-subheader>
      <v-list-item
          v-for="item in orders"
          :title="item.menuByMenuId.name"
          :subtitle="item.orderedAt"
      >
      </v-list-item>
    </v-list>
  </v-card>
  <v-snackbar v-model="snack" :color="color" :timeout="3000">{{ snackText }}</v-snackbar>
</template>

<script>
import _ from "lodash";
import * as yup from "yup";
import server from "@/business/PizzaServerAPI";

const defaultItem = {
  id: 0,
  email: "",
  name: "",
  password: "",
  streetNameByStreetNameId: {
    id: ""
  },
  roleByRoleId: {id: 0},
  houseNo: 0,
}
export default {
  name: "Registration",
  inject: ['logout', 'role'],
  data() {
    return {
      orders: [],
      streets: [],
      editedItem: _.cloneDeep(defaultItem),
      schema: yup.object({
        email: yup.string().required().email(),
        name: yup.string().required(),
        password: yup.string().required().min(8)
      }),
      errors: {},
      snack: false,
      snackText: '',
      color: '',
    };
  },
  mounted() {
    server.readStreets().then(promise => this.streets = promise.data)

    if (!server.id()) return;
    server.findUser().then(promise => {
      this.editedItem = {...promise.data, password: ''}

      if (!this.editedItem.streetNameByStreetNameId) {
        this.editedItem = {...this.editedItem, streetNameByStreetNameId: {id: ''}}
      }
    })
    server.readOrders().then(promise => this.orders = promise.data)
  },
  methods: {
    async save() {
      this.errors = {}
      let vueBug = null

      await this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            if (this.editedItem.streetNameByStreetNameId.id) {
              const house = parseInt(this.editedItem.houseNo)
              if (!isNaN(house)) {
                const streetMax = this.streets
                    .filter(street => street.id === this.editedItem.streetNameByStreetNameId.id)[0].untilNo
                if (house > streetMax) {
                  this.errors = {houseNo: "Unknown address"}
                  return
                }
              } else {
                this.errors = {houseNo: "Must be a number"}
                return;
              }
            } else {
              vueBug = {
                id: this.editedItem.id,
                name: this.editedItem.name,
                email: this.editedItem.email,
                password: this.editedItem.password,
                roleByRoleId: {id: this.editedItem.roleByRoleId.id}
              }
            }
          }).catch(err => {
            err.inner.forEach((error) => {
              this.errors = {...this.errors, [error.path.split('.')[0]]: error.message};
            });
          });


      if (!Object.keys(this.errors).length && !this.editedItem.id) {
        console.log(vueBug)
        // role 0 will convert to customer at the backend
        server.registerUser(vueBug ? vueBug : this.editedItem)
            .then(promise => {
              if (promise.status === 201) {
                this.snackText = "Created"
                this.color = "green"
                this.snack = true
              }
            }).catch(err => {
          if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
            this.snackText = "Email is already linked to a account"
            this.color = "red"
            this.snack = true
          }
        })

        this.editedItem = _.cloneDeep(defaultItem)

      } else if (!Object.keys(this.errors).length) {
        server.updateUser(vueBug ? vueBug : this.editedItem)
            .then(promise => {
              if (promise.status === 201) {
                this.snackText = "Updated"
                this.color = "green"
                this.snack = true
              }
            }).catch(err => {
          if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
            this.snackText = "Email is already linked to a account"
            this.color = "red"
            this.snack = true
          }
        })

        if (!this.editedItem.streetNameByStreetNameId) {
          this.editedItem = {...this.editedItem, streetNameByStreetNameId: {id: ''}, houseNo: 0}
        }
      }
    },
    async erase() {
      if (confirm('You are about to permanently delete your account. Are you sure?')) {
        await server.deleteUser(server.id()).then((promise) => {
          if (promise.status === 200) {
            this.snackText = "Deleted"
            this.color = "green"
            this.snack = true
          }
        }).catch(err => {
          if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
            this.snackText = "Still linked to a car"
            this.color = "red"
            this.snack = true
          }
        });
        await this.logout()
        this.$router.push("/")
      }
    }
  }
}
</script>