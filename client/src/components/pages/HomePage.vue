<template>
  <h2>{{ welcome }}</h2>
  <v-window show-arrows="hover" v-model="stepper" class="window">
    <template v-slot:prev="{ props }">
      <v-btn
          prepend-icon="mdi-chevron-left"
          @click="props.onClick"
          rounded="xl"
          :disabled="time > 0"
      >
        Back
      </v-btn>
    </template>
    <template v-slot:next="{ props }">
      <v-btn
          append-icon="mdi-chevron-right"
          @click="next(props)"
          rounded="xl"
          :disabled="stepper === 3 && !checkbox || !stepper && !basket.length"
      >
        {{ stepperNext }}
      </v-btn>
    </template>
    <v-window-item
        key="Basket"
    >
      <v-card
          elevation="2"
          height="200"
          class="d-flex align-center justify-center ma-2"
      >
        <h3
            class="text-h3"
        >
          {{ basketStatus }}
        </h3>
      </v-card>
    </v-window-item>
    <v-window-item
        key="Order"
    >
      <v-card
          elevation="2"
          height="200"
          class="d-flex align-center justify-center ma-2"
      >
        <v-list>
          <v-list-subheader>Basket</v-list-subheader>

          <v-list-item
              v-for="item in basketItems"
              :key="item.id"
              :value="item.id"
              :title="item.name"
              active-color="primary"
              @click="add(item.id)"
          >
            <template v-slot:append>
              &nbsp;- {{ basket.filter(i => i.id === item.id).length }}x
            </template>
            <!--            <v-list-item-title v-text="item.name"></v-list-item-title>-->
          </v-list-item>
        </v-list>
      </v-card>
    </v-window-item>
    <v-window-item
        key="Delivery"
    >
      <v-card
          elevation="2"
          height="200"
          class="d-flex align-center justify-center ma-2 stepper-card"
      >
        <v-autocomplete
            clearable
            class="padding"
            label="Available streets"
            name="street"
            :items="streets"
            item-title="that"
            item-value="id"
            v-model="address.streetNameByStreetNameId.id"
            :error-messages="errors.street"
        ></v-autocomplete>
        <v-text-field style="max-width: 200px"
                      label="House Number" name="house"
                      v-model="address.houseNo" :error-messages="errors.house"/>
      </v-card>
    </v-window-item>
    <v-window-item
        key="Summary"
    >
      <v-card
          elevation="2"
          height="200"
          class="d-flex align-center justify-center ma-2 stepper-card"
      >
        <v-container>
          <v-row>
            <h3
                class="text-h3"
            >
              Total: ${{ basket.reduce((acc, curr) => acc + curr.price, 0) }}
            </h3>
          </v-row>
          <v-row>
            <v-checkbox v-model="checkbox" label="Agree to Terms and Conditions"/>
          </v-row>
        </v-container>
      </v-card>
    </v-window-item>
    <v-window-item
        key="Finish"
    >
      <v-card
          elevation="2"
          height="200"
          class="d-flex align-center justify-center ma-2"
      >
        <h3
            class="text-h3"
        >
          Your order is getting delivered!
          {{ confetti }}
        </h3>
      </v-card>
    </v-window-item>
  </v-window>
  <!--  <Map/>-->
  <v-snackbar v-model="snack" :timeout="3000">{{ snackText }}</v-snackbar>
</template>

<script>
import Map from "@/components/Map.vue";
import server from "@/business/PizzaServerAPI.vue"
import {id} from "@/business/PizzaServerAPI.vue";
import _ from "lodash";
import JSConfetti from 'js-confetti'

const confetti = new JSConfetti()
export default {
  name: "HomePage",
  components: {Map},
  inject: ['orders', 'setOrders', 'role'],
  data() {
    return {
      stepper: 0,
      checkbox: false,
      address: {
        streetNameByStreetNameId: {
          id: 1
        },
        houseNo: 1
      },
      errors: {},
      basket: [],
      streets: [],
      user: {},
      time: 0,
      snack: false,
      snackText: ""
    }
  },
  methods: {
    //does not let qt per menu to grow over 5
    add(id) {
      console.log(id)
      const items = this.basket.filter(i => i.id === id)
      if (items.length >= 5)
        this.basket = this.basket.filter(item => item.id !== items[0].id)

      this.basket.push(items[0])
      console.log(this.basket)
    },
    async next(props) {
      //only validates at the third card
      if (this.stepper === 2) {
        this.errors = {}
        if (this.address.streetNameByStreetNameId.id) {
          const house = parseInt(this.address.houseNo)
          if (!isNaN(house)) {
            const streetMax = this.streets.filter(street => street.id === this.address.streetNameByStreetNameId.id)[0].untilNo
            if (house <= streetMax) {
              props.onClick()
              return
            } else {
              this.errors = {house: "Unknown address"}
            }
          } else {
            this.errors = {house: "Must be a number"}
          }
        } else {
          this.errors = {street: "Missing street"}
        }

      } else if (this.stepper === 3) {
        let userByUserId = {...this.user, ...this.address}
        delete userByUserId.foodOrdersById
        //cannot be deserialized on server-side if contains other references for menu like the current
        const orders = this.basket.map(menuByMenuId => ({
          menuByMenuId,
          userByUserId
        }))
        server.placeOrder(orders).then(promise => {
          this.time = promise.data
          if (this.time) {
            this.setOrders([])
            this.basket = []
            props.onClick()
          } else {
            this.snackText = "Order cannot be delivered at the moment"
            this.snack = true
          }
        })
      } else if (this.stepper < 2) {
        props.onClick()
      }
    }
  },
  computed: {
    welcome() {
      return this.role.length ? "Welcome back!" : "Login first!"
    },
    stepperNext() {
      switch (this.stepper) {
        case 0:
          return 'Basket';
        case 1:
          return 'Delivery';
        case 2:
          return 'Summary';
        case 3:
          return 'Send';
      }
    },
    basketStatus() {
      if (!this.basket.length)
        return "Pick from Menu 🍕"
      if (this.basket.length === 1)
        return "You selected 1 menu"

      return `You selected ${this.basket.length} menus`
    },
    basketItems() {
      console.log(_.uniqBy(this.basket, 'name'))
      return _.uniqBy(this.basket, 'name')
    },
    confetti() {
      confetti.addConfetti()
    },
  },
  mounted() {
    if (!id()) return;

    server.findUser().then(promise => this.user = promise.data)
    this.orders.forEach(id => server.findMenu(id)
        .then(promise => this.basket.push(promise.data)).catch(err => console.log(err)))
    server.readStreets().then(promise => this.streets = promise.data)
  }
}
</script>

<style scoped>
.padding {
  padding-left: 25px;
  padding-right: 25px;
}

.stepper-card {
  padding-left: 100px;
  padding-right: 150px;
}
</style>