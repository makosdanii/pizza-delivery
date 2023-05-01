<template>
  <v-navigation-drawer
      expand-on-hover
      rail
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
          v-if="role === 'admin'"
          @click="$router.push('/user')"
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
          v-if="role === 'admin'"
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
    </v-list>
  </v-navigation-drawer>
</template>

<script>
export default {
  name: "MenuDrawer",
  inject: ['role']
}
</script>

<style scoped>

</style>