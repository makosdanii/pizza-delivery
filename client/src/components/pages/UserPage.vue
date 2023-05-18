<template>
  <DataTable
      name="User"
      :headers="headers"
      :items="users"
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
            <v-text-field name="email" v-model="editedItem.email" label="Email" :error-messages="errors.email"/>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field name="name" v-model="editedItem.name" label="Name" :error-messages="errors.name"/>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-text-field
                name="password"
                type="password"
                v-model="editedItem.password"
                label="Password"
                :error-messages="errors.password"/>
          </v-col>
          <v-col cols="12" sm="6" md="4">
            <v-select
                :items="roles"
                item-title="name"
                item-value="id"
                label="Select"
                name="roleByRoleId"
                v-model="editedItem.roleByRoleId.id"
                :error-messages="errors.roleByRoleId"
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
  email: "",
  name: "",
  password: "",
  roleByRoleId: {
    id: ""
  },
}

export default {
  name: "UserPage",
  components: {
    DataTable
  },

  inject: ['users', 'roles', 'listRoles', 'listUsers'],

  methods: {
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            !this.editedItem.id ?
                server.registerUser(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listUsers()
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
                server.updateUser(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listUsers()
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
              this.errors = {...this.errors, [error.path.split('.')[0]]: error.message};
            });
          });
    },
    edit(item) {
      this.editedItem = _.cloneDeep(this.users.filter(user => user.id === item.id)[0])
      this.$refs.table.dialog = true
    },
    erase(item) {
      if (confirm(`This user will be deleted`)) {
        server.deleteUser(item.id).then((promise) => {
          if (promise.status === 200) {
            this.listUsers()
            this.$refs.table.snackText = "Deleted"
            this.$refs.table.color = "green"
            this.$refs.table.snack = true
          }
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
        {title: "Email", key: "email"},
        {title: "Name", key: "name"},
        {title: "Role", key: "roleByRoleId.name"},
        {title: "Actions", key: "actions", sortable: false},
      ],
      editedItem: _.cloneDeep(defaultItem),
      schema: yup.object({
        email: yup.string().required().email(),
        name: yup.string().required(),
        password: yup.string().required().min(8),
        roleByRoleId: yup.object({
          id: yup.number().required("Missing role")
        })
      }),
      errors: {}
    };
  },

  async mounted() {
    await this.listUsers()
    await this.listRoles()
  },
};
</script>
