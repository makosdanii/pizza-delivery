<template>
  <DataTable
      name="Menu"
      :headers="headers"
      :items="menus"
      :expand="true"
      :cannot-create="role !== 'admin' && role !== 'chef'"
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
            <v-text-field
                name="price"
                type="number"
                v-model="editedItem.price"
                label="Price"
                :error-messages="errors.price"/>
          </v-col>
        </v-row>
      </v-container>
    </template>
    <template v-slot:expand="{item, width}">
      <MenuExpandItem @updated="refresh" :width="width" :id="item.id"
                      :menu-ingredients-by-id="item.menuIngredientsById"/>
    </template>
  </DataTable>
</template>

<script>
import server from "../../business/PizzaServerAPI.vue";
import DataTable from "../DataTable.vue";
import * as yup from "yup";
import _ from "lodash";
import ingredientPage from "@/components/pages/IngredientPage.vue";
import MenuExpandItem from "@/components/MenuExpandItem.vue";

const defaultItem = {
  id: 0,
  name: "",
  price: 0
}

export default {
  name: "MenuPage",
  computed: {
    ingredientPage() {
      return ingredientPage
    }
  },
  components: {
    MenuExpandItem,
    DataTable
  },

  inject: ['ingredients', 'listIngredients', 'role'],

  methods: {
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            !this.editedItem.id ?
                server.addMenu(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        server.listMenus().then(promise => this.menus = promise.data)
                        this.$refs.table.snackText = "Created"
                        this.$refs.table.snack = true
                      }
                    })
                :
                server.updateMenu(this.editedItem)
                    .then((promise) => {
                      if (promise.status === 201) {
                        server.listMenus().then(promise => this.menus = promise.data)
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
      this.editedItem = _.cloneDeep(this.menus.filter(menu => menu.id === item.id)[0])
      this.$refs.table.dialog = true
    },
    erase(item) {
      if (confirm(`This menu item will be deleted`)) {
        server.deleteMenu(item.id).then((promise) => {
          if (promise.status === 200) {
            server.listMenus().then(promise => this.menus = promise.data)
            this.$refs.table.snackText = "Deleted"
            this.$refs.table.snack = true
          }
        });
      }
    },
    reset() {
      this.editedItem = _.cloneDeep(defaultItem)
      this.$refs.table.dialog = false;
    },
    refresh() {
      //memory allocated for the menus only needed during components lifecycle, no need to store in context
      server.listMenus().then(promise => this.menus = promise.data)
    }
  },

  data() {
    return {
      headers: [],
      editedItem: _.cloneDeep(defaultItem),
      schema: yup.object({
        name: yup.string().required(),
        price: yup.number().required().min(1),
      }),
      menus: [],
      errors: {}
    };
  },

  mounted() {
    let authorized = false
    if (this.role === 'admin' || this.role === 'chef') {
      this.headers = [{title: "Actions", key: "actions", sortable: false}]
      authorized = true;
    }
    if (this.role.length || authorized) {
      this.headers = [{title: "Add", key: "id"}, ...this.headers]
    }
    this.headers = [
      {title: "Name", key: "name"},
      {title: "Price", key: "price"},
      ...this.headers,
      {title: "Details", key: 'data-table-expand'},
    ]
    this.refresh()
  },
};
</script>
