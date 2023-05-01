<template>
  <form @submit.prevent="save">
    <v-container>
      <v-row>
        <v-text-field v-if="!name.length"
                      class="short margin"
                      name="email" label="Email"
                      :error-messages="errors.email"
                      v-model="editedItem.email"/>
        <v-text-field v-if="!name.length"
                      class="short margin"
                      name="password"
                      type="password"
                      label="Password" v-model="editedItem.password"
                      :error-messages="errors.password"/>
        <v-spacer/>
        <v-btn class="mr-4 margin" v-if="orders.length" prepend-icon="mdi-cart"
               @click="$router.push('/')"/>
        <v-btn class="mr-4 margin" color="primary" type="submit">
          {{ name.length ? "Logout" : "Login" }}
        </v-btn>
      </v-row>
    </v-container>
  </form>
  <v-divider inset horizontal/>
  <v-snackbar v-model="snack" :timeout="3000">{{ snackText }}</v-snackbar>
</template>

<script>
import * as yup from 'yup';
import _ from "lodash";
import server from "@/business/PizzaServerAPI.vue";
import {id} from "@/business/PizzaServerAPI.vue";

const defaultItem = {
  email: "",
  password: "",
}

export default {
  name: "LoginPage",
  data() {
    return {
      snack: false,
      snackText: "",
      schema: yup.object({
        email: yup.string().required().email(),
        password: yup.string().required(),
      }),
      name: "",
      editedItem: _.cloneDeep(defaultItem),
      errors: {}
    }
  },

  inject: ['login', 'logout', 'orders'],

  methods: {
    async save() {
      if (!this.name.length) {
        await this.login({...this.editedItem, rememberMe: false})
        await server.findUser()
            .then(promise => this.name = promise.data.name)
            .catch(err => console.log(err))
        this.snackText = `Login ${(this.name.length ? "succeeded" : "failed")}`
        this.snack = true;
        this.errors = {}
        await this.schema.validate(this.editedItem, {abortEarly: false})
            .catch((err) => {
              err.inner.forEach((error) => {
                this.errors = {...this.errors, [error.path]: error.message};
              });
            });

        if (Object.keys(this.errors).length) return

      } else {
        if (await this.logout()) {
          this.name = ''
          this.snack = true;
          this.snackText = `Logged out`
          this.$router.push('/')
        }
      }

      this.editedItem = _.cloneDeep(defaultItem)
    },
  },
  mounted() {
    if (id())
      server.findUser()
          .then(promise => this.name = promise.data.name)
          .catch(err => console.log(err))
  }
}
</script>
<style>
.hidden {
  visibility: hidden;
}

.short {
  max-width: 200px;
}

.margin {
  margin: 10px;
}
</style>
