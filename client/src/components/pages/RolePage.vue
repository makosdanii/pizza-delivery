<template>
  <DataTable
      name="Role"
      :headers="headers"
      :items="roles"
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
import server from "../../business/PizzaServerAPI.vue";
import DataTable from "../DataTable.vue";
import * as yup from "yup";
import _ from "lodash";

const defaultItem = {
  id: 0,
  name: ""
}

export default {
  name: "RolePage",
  components: {
    DataTable,
  },

  inject: ['roles', 'listRoles'],

  methods: {
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            !this.editedItem.id ?
                server.addRole(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listRoles()
                        this.$refs.table.snackText = "Created"
                        this.$refs.table.snack = true
                      }
                    })
                :
                server.updateRole(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        this.listRoles()
                        this.$refs.table.snackText = "Updated"
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
      this.editedItem = _.cloneDeep(this.roles.filter(role => role.id === item.id)[0])
      this.$refs.table.dialog = true
    },
    erase(item) {
      if (confirm(`This role will be deleted`)) {
        server.deleteRole(item.id).then((promise) => {
          if (promise.status === 200) {
            this.listRoles()
          }
          this.$refs.table.snackText = "Deleted"
          this.$refs.table.snack = true
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
    await this.listRoles()
  },
};
</script>
