<template>
  <v-tabs
      v-model="tab"
      bg-color="primary"
  >
    <v-tab value="inventory">Inventory</v-tab>
    <v-tab value="orders">Orders</v-tab>
    <v-tab value="deliveries">Deliveries</v-tab>
  </v-tabs>

  <v-card-text>
    <v-window v-model="tab">
      <v-window-item value="inventory">
        <DataTable
            :headers="headers"
            :items="items"
            :cannot-update="true"
            @save="save"
            @cancel="reset"
            @delete="eraseInventory"
            ref="table">

          <template #card-container>
            <v-container>
              <v-row>
                <v-col cols="12" sm="6" md="4">
                  <v-select
                      :items="ingredients"
                      item-title="name"
                      item-value="id"
                      label="Select"
                      name="ingredientByIngredientId"
                      v-model="editedItem.ingredientByIngredientId.id"
                      :error-messages="errors.ingredientByIngredientId"
                  ></v-select>
                </v-col>
                <v-col cols="12" sm="6" md="4">
                  <v-text-field type="number" name="current" label="Additional quantity"
                                v-model="editedItem.current"
                                :error-messages="errors.current"/>
                </v-col>
              </v-row>
            </v-container>
          </template>
        </DataTable>
      </v-window-item>

      <v-window-item value="orders">
        <DataTable
            :headers="headers"
            :items="items"
            :cannot-create="true"
            :cannot-update="true"
            @save="save"
            @delete="eraseOrder"
            ref="table2"/>
      </v-window-item>

      <v-window-item value="deliveries">
        <DataTable
            :headers="headers"
            :items="items"
            :cannot-create="true"
            ref="table3"/>
      </v-window-item>
    </v-window>
  </v-card-text>
</template>

<script>
import DataTable from "@/components/DataTable.vue";
import server from "@/business/PizzaServerAPI.js"
import * as yup from "yup";

export default {
  name: "HistoryPage",
  components: {DataTable},
  inject: ['ingredients', 'listIngredients'],
  data() {
    return {
      tab: null,
      headers: [],
      items: [],
      editedItem: {
        ingredientByIngredientId: {
          id: ""
        },
        current: 0
      },
      schema: yup.object({
        ingredientByIngredientId: yup.object({
          id: yup.number().required(),
        }),
        current: yup.number().min(1).required()
      }),
      errors: {}
    }
  },
  methods: {
    save() {
      this.errors = {}
      this.schema.validate(this.editedItem, {abortEarly: false})
          .then(() => {
            const ingredientPrice = this.ingredients.filter(ingredient => ingredient.id === this.editedItem.ingredientByIngredientId.id)[0].price
            server.addInventory({
              ...this.editedItem,
              expense: ingredientPrice * this.editedItem.current,
            })
                .then((promise) => {
                  if (promise.status === 201) {
                    server.readInventory().then(promise => this.items = promise.data)
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
            this.reset()
          })
          .catch((err) => {
            err.inner.forEach((error) => {
              this.errors = {...this.errors, [error.path]: error.message};
            });
          });
    },
    eraseInventory(item) {
      server.deleteInventory(item.ingredientByIngredientId.id
      )
          .then(promise => this.items = promise.data)
          .catch(err => {
            if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
              this.$refs.table.snackText = "Operation denied"
              this.$refs.table.color = "red"
              this.$refs.table.snack = true
            }
          })
    },
    eraseOrder(item) {
      server.deleteOrder(item.id)
          .then(promise => {
            if (promise.status !== 404)
              this.items = promise.data.map(item => ({...item, number: item.id}))
          })
          .catch(err => {
            if (err.response.status === 400 || err.response.status === 401 || err.response.status === 404) {
              this.$refs.table.snackText = "Operation denied"
              this.$refs.table.color = "red"
              this.$refs.table.snack = true
            }
          })
    },
    reset() {
      this.editedItem = {
        ingredientByIngredientId: {
          id: ""
        },
        quantity: 0
      }
      this.$refs.table.dialog = false
    }
  },
  watch: {
    tab() {
      switch (this.tab) {
        case 'inventory':
          server.readInventory()
              .then(promise => this.items = promise.data.length > 100 ? promise.data.slice(-100) : promise.data)
          this.listIngredients()
          this.headers = [
            {title: "Modified at:", key: "modifiedAt"},
            {title: "Ingredient", key: "ingredientByIngredientId.name"},
            {title: "Car", key: "carByCarId.license"},
            {title: "Current quantity", key: "current"},
            {title: "Expense", key: "expense"},
            {title: "Actions", key: "actions", sortable: false}]
          break;
        case 'orders':
          server.readOrders().then(promise => this.items = promise.data.length > 100 ?
              promise.data.slice(-100).map(item => ({...item, number: item.id})) :
              promise.data.map(item => ({...item, number: item.id})))
          this.headers = [
            {title: "Ordered at:", key: "orderedAt"},
            {title: "#", key: "number"},
            {title: "Menu", key: "menuByMenuId.name"},
            {title: "Actions", key: "actions", sortable: false}]

          break;
        case 'deliveries':
          server.readDeliveries()
              .then(promise => this.items = promise.data.length > 100 ? promise.data.slice(-100) : promise.data)
          this.headers = [
            {title: "Delivered at:", key: "deliveredAt"},
            {title: "Order ID", key: "foodOrderByFoodOrderId.id"},
            {title: "For", key: "foodOrderByFoodOrderId.userByUserId.name"},
            {title: "By", key: "carByCarId.license"}]
          break;
      }
    }
  }
}
</script>

<style scoped>

</style>