<template>
  <v-navigation-drawer
      expand-on-hover
      rail
      @update:rail="() => mini = !mini"
  >
    <v-list nav dense>
      <v-list-item
          @click="$router.push('/')"
          link
          prepend-icon="mdi-home"
      >
        <v-list-item-title>Home</v-list-item-title>
      </v-list-item>

      <v-divider v-if="role !== 'driver'" class="mx-4" inset horizontal></v-divider>

      <v-list-item
          v-if="role !== 'driver'"
          @click="$router.push('/menu')"
          link
          prepend-icon="mdi-book-open-variant"
      >
        <v-list-item-title>Food menus</v-list-item-title>
      </v-list-item>

      <v-divider v-if="role === 'admin'" class="mx-4" inset horizontal></v-divider>

      <v-list-item
          v-if="role"
          @click="role === 'admin' ? $router.push('/users') : $router.push('/user')"
          link
          prepend-icon="mdi-account"
      >
        <v-list-item-title>User account</v-list-item-title>
      </v-list-item>
      <v-list-item
          v-if="role === 'admin'"
          @click="$router.push('/role')"
          link
          prepend-icon="mdi-shield-lock"
      >
        <v-list-item-title>User roles</v-list-item-title>
      </v-list-item>
      <v-list-item
          v-if="role === 'admin' || role === 'driver'"
          @click="$router.push('/car')"
          link
          prepend-icon="mdi-truck-fast"
      >
        <v-list-item-title>Car fleet</v-list-item-title>
      </v-list-item>

      <v-divider v-if="role === 'chef' || role === 'admin'" class="mx-4" inset horizontal></v-divider>

      <v-list-item
          v-if="role === 'chef' || role === 'admin'"
          @click="$router.push('/allergy')"
          link
          prepend-icon="mdi-peanut"
      >
        <v-list-item-title>Ingredient allergy</v-list-item-title>
      </v-list-item>
      <v-list-item
          v-if="role === 'chef' || role === 'admin'"
          @click="$router.push('/ingredient')"
          link
          prepend-icon="mdi-food-variant"
      >
        <v-list-item-title>Food ingredients</v-list-item-title>
      </v-list-item>

      <v-divider v-if="role !== 'chef' && role.length" class="mx-4" inset horizontal></v-divider>

      <v-list-item
          v-if="role === 'admin' || role === 'customer'"
          @click="$router.push('/history')"
          link
          prepend-icon="mdi-history"
      >
        <v-list-item-title>Activity history</v-list-item-title>
      </v-list-item>
      <v-list-item>
        <v-dialog
            width="500px"

        >
          <template v-slot:activator="{ props }">
            <v-btn v-if="role === 'admin' && !mini" icon="mdi-microsoft-excel" variant="tonal" color="green-darken-4"
                   v-bind="props"/>
          </template>
          <v-card height="350px">
            <h2 class="margin">Print records before:</h2>
            <vue3-datepicker-esm class="margin" v-model="picked"/>
            <v-spacer/>
            <v-btn @click="download">Download</v-btn>
          </v-card>
        </v-dialog>
      </v-list-item>
      <v-list-item>
        <v-btn v-if="role === 'admin' && !mini" icon="mdi-restart" variant="outlined" color="grey-darken-1"
               @click="restart" :loading="loading"/>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
import server from '@/business/PizzaServerAPI'
import exporting from '@/business/PizzaServerExport'
import Vue3DatepickerEsm from "vue3-datepicker";
import {date} from "yup";

export default {
  name: "MenuDrawer",
  inject: ['role'],
  components: {Vue3DatepickerEsm},
  data() {
    return {
      mini: true,
      loading: false,
      picked: new Date()
    }
  },
  methods: {
    date,
    restart() {
      this.loading = true
      server.restart().then(promise => setTimeout(() => (this.loading = promise > 0), 1500)).catch(err => {
        console.log(err)
        this.loading = false
      })
    },
    download() {
      exporting.exportToSpreadsheet(this.picked)
    }
  }
}
</script>

<style scoped>

</style>