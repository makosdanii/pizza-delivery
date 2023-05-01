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
            :cannot-create="true"
            :cannot-update="true"
            @delete="eraseInventory"/>
      </v-window-item>

      <v-window-item value="orders">
        <DataTable
            :headers="headers"
            :items="items"
            :cannot-create="true"
            :cannot-update="true"
            @delete="eraseOrder"/>
      </v-window-item>

      <v-window-item value="deliveries">
        <DataTable
            :headers="headers"
            :items="items"
            :cannot-create="true"
            :cannot-update="true"
            :cannot-delete="true"/>
      </v-window-item>
    </v-window>
  </v-card-text>
</template>

<script>
import DataTable from "@/components/DataTable.vue";
import server from "@/business/PizzaServerAPI.vue"

export default {
  name: "HistoryPage",
  components: {DataTable},
  data() {
    return {
      tab: null,
      headers: [],
      items: [],
    }
  },
  methods: {
    eraseInventory(item) {
      server.deleteInventory({carByCarId: {id: item.carByCarId.id}, modifiedAt: item.modifiedAt})
          .then(promise => this.items = promise.data).catch(err => console.log(err))
    },
    eraseOrder(item) {
      server.deleteOrder(item.id)
          .then(promise => {
            if (promise.status !== 404)
              this.items = promise.data
          })
          .catch(err => console.log(err))
    }
  },
  watch: {
    tab() {
      switch (this.tab) {
        case 'inventory':
          server.readInventory().then(promise => this.items = promise.data)
          this.headers = [{title: "Expense", key: "expense"},
            {title: "Car", key: "carByCarId.license"},
            {title: "Ingredient", key: "ingredientByIngredientId.name"},
            {title: "Current quantity", key: "currentQt"},
            {title: "Modified at:", key: "modifiedAt"},
            {title: "Actions", key: "actions", sortable: false}]
          break;
        case 'orders':
          server.readOrders().then(promise => this.items = promise.data)
          this.headers = [{title: "User", key: "userByUserId.name"},
            {title: "Menu", key: "menuByMenuId.name"},
            {title: "Ordered at:", key: "orderedAt"},
            {title: "Actions", key: "actions", sortable: false}]

          break;
        case 'deliveries':
          server.readDeliveries().then(promise => this.items = promise.data)
          this.headers = [
            {title: "Car", key: "carByCarId.license"},
            {title: "Order ID", key: "foodOrderByFoodOrderId"},
            {title: "Delivered at:", key: "deliveredAt"},
            {title: "Actions", key: "actions", sortable: false}]
          break;
      }
    }
  }
}
</script>

<style scoped>

</style>