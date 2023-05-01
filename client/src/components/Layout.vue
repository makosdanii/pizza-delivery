<template>
  <v-app>
    <LoginPage/>
    <MenuDrawer/>
    <v-main>
      <v-container fluid style="position: relative">
        <v-card class="overflow-hidden" style="position: relative">
          <router-view/>
        </v-card>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import MenuDrawer from "@/components/MenuDrawer.vue";
import LoginPage from "@/components/pages/LoginPage.vue";

export default {
  name: "Layout",
  components: {LoginPage, MenuDrawer}
}
</script>

<style scoped>

</style>