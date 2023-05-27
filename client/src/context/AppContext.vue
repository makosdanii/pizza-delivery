<template>
  <div id="context-provider">
    <slot></slot>
  </div>
</template>

<script>
import server from "../business/PizzaServerAPI.js";
import {computed} from "vue";

export default {
  name: "AppContext",
  data() {
    return {
      role: "",
      users: [],
      roles: [],
      allergies: [],
      cars: [],
      ingredients: [],
      orders: [],
    }
  },

  provide() {
    return {
      role: computed(() => this.role),
      users: computed(() => this.users),
      roles: computed(() => this.roles),
      cars: computed(() => this.cars),
      allergies: computed(() => this.allergies),
      ingredients: computed(() => this.ingredients),
      orders: computed(() => this.orders),
      setOrders: (orders) => this.setOrders(orders),
      listUsers: this.listUsers,
      listRoles: this.listRoles,
      listCars: this.listCars,
      listAllergies: this.listAllergies,
      listIngredients: this.listIngredients,
      login: this.login,
      logout: this.logout,
    }
  },
  mounted() {
    if (this.$cookies.isKey('role'))
      this.role = this.$cookies.get('role')
  },
  methods: {
    // cannot use arrow function if 'this' is needed in the scope
    async login(values) {
      await server.authenticate(values)
          .then(promise => {
            this.$cookies.set('userId', promise.data.id, '5h')
            this.$cookies.set('token', promise.data.token, '5h')
          }).catch((err) => console.log(err))
      await server.findUser().then(promise => {
        this.role = promise.data.roleByRoleId.name;
        this.$cookies.set('role', this.role, '5h')
      }).catch((err) => console.log(err))
    },
    async logout() {
      // await server.unset().then(promise => {
      //   if (promise.status === 200) {
      this.users = this.roles = this.cars = this.allergies = this.ingredients = this.orders = []
      this.role = ""
      this.$cookies.remove('userId')
      this.$cookies.remove('role')
      this.$cookies.remove('token')
      //   }
      // }).catch((err) => console.log(err));
      return !this.role.length
    },
    async listUsers() {
      await server.listUsers().then(promise => this.users = promise.data).catch((err) => console.log(err))
    },
    async listRoles() {
      await server.listRoles().then(promise => this.roles = promise.data.filter(role => role.name !== 'admin')).catch((err) => console.log(err))
    },
    async listCars() {
      await server.listCars().then(promise => this.cars = promise.data).catch((err) => console.log(err))
    },
    async listAllergies() {
      await server.listAllergies().then(promise => this.allergies = promise.data).catch((err) => console.log(err))
    },
    async listIngredients() {
      await server.listIngredients().then(promise => this.ingredients = promise.data.filter(ingredient => ingredient.name !== 'fuel')).catch((err) => console.log(err))
    },
    setOrders(orders) {
      this.orders = orders
    }
  },
}
</script>