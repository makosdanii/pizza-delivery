import {createApp} from 'vue'
import {createVuetify} from 'vuetify'
import * as components from 'vuetify/components'
import * as lab from "vuetify/labs/components";
import * as directives from 'vuetify/directives'
import * as VueRouter from 'vue-router';
import VueCookies from 'vue-cookies';

import App from './App.vue'
import HomePage from "@/components/pages/HomePage.vue";
import MenuPage from "@/components/pages/MenuPage.vue";
import UserPage from "@/components/pages/UserPage.vue";
import RolePage from "@/components/pages/RolePage.vue";
import CarPage from "@/components/pages/CarPage.vue";
import AllergyPage from "@/components/pages/AllergyPage.vue";
import IngredientPage from "@/components/pages/IngredientPage.vue";
import HistoryPage from "@/components/pages/HistoryPage.vue";
import LoginPage from "@/components/pages/LoginPage.vue";
import RegistrationPage from "@/components/pages/RegistrationPage.vue";
import _ from 'lodash';
import server from "@/business/PizzaServerAPI.js";

import 'vuetify/styles'
import './assets/main.css'
import "material-design-icons-iconfont/dist/material-design-icons.css";
import "@mdi/font/css/materialdesignicons.css";

const routes = [
    {path: '/', component: HomePage},
    {path: '/menu', component: MenuPage},
    {path: '/user', component: RegistrationPage},
    {
        path: '/users', component: UserPage, beforeEnter: (to, from) => {
            if (server.role() !== 'admin' && server.role() !== 'customer')
                return '/'
        }
    },
    {
        path: '/role', component: RolePage, beforeEnter: (to, from) => {
            if (server.role() !== 'admin')
                return '/'
        }
    },
    {
        path: '/car', component: CarPage, beforeEnter: (to, from) => {
            if (server.role() !== 'admin' && server.role() !== 'driver')
                return '/'
        }
    },
    {
        path: '/allergy', component: AllergyPage, beforeEnter: (to, from) => {
            if (server.role() !== 'admin' && server.role() !== 'chef')
                return '/'
        }
    },
    {
        path: '/ingredient', component: IngredientPage, beforeEnter: (to, from) => {
            if (server.role() !== 'admin' && server.role() !== 'chef')
                return '/'
        }
    },
    {
        path: '/history', component: HistoryPage, beforeEnter: (to, from) => {
            if (server.role() !== 'admin' && server.role() !== 'customer')
                return '/'
        }
    },
]

const router = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
    routes
})

const vuetify = createVuetify({
    components: {...components, ...lab},
    directives,
})

const app = createApp(App)
app.config.unwrapInjectedRef = true

app.use(router)
    .use(vuetify)
    .use(VueCookies)
    .use(_)
    .mount('#app')

