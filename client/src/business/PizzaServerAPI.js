import axios from "axios";
import VueCookies from 'vue-cookies';

const SERVER_URL = "http://localhost:8081";
// axios.defaults.withCredentials = true

const instance = axios.create({
    baseURL: SERVER_URL,
    timeout: 3000,
});

const config = () => ({
    headers: {Authorization: VueCookies.isKey('token') ? `Bearer ${VueCookies.get('token')}` : ''}
})

const id = () => VueCookies.isKey('userId') ? VueCookies.get('userId') : 0

const role = () => VueCookies.isKey('role') ? VueCookies.get('role') : ''

export default {
    id: () => id(),
    role: () => role(),
    authenticate: async (values) => await instance.post("authenticate", values),
    unset: async () => await instance.get("unset", config()),
    findUser: async () => await instance.get(`user/${id()}`, config()),
    listUsers: async () => await instance.get('user', config()),
    registerUser: async (values) => await instance.post('user/register', values, config()),
    updateUser: async (values) => await instance.put(`user/${values.id}`, values, config()),
    deleteUser: async (id) => await instance.delete(`user/${id ? id : id()}`, config()),
    listRoles: async () => await instance.get("role", config()),
    addRole: async (values) => await instance.post('role', values, config()),
    updateRole: async (values) => await instance.put(`role/${values.id}`, values, config()),
    deleteRole: async (id) => await instance.delete(`role/${id}`, config()),
    listCars: async () => await instance.get("car", config()),
    addCar: async (values) => await instance.post('car', values, config()),
    updateCar: async (values) => await instance.put(`car/${values.id}`, values, config()),
    deleteCar: async (id) => await instance.delete(`car/${id}`, config()),
    listAllergies: async () => await instance.get("allergy", config()),
    addAllergy: async (values) => await instance.post('allergy', values, config()),
    updateAllergy: async (values) => await instance.put(`allergy/${values.id}`, values, config()),
    deleteAllergy: async (id) => await instance.delete(`allergy/${id}`, config()),
    listIngredients: async () => await instance.get("ingredient", config()),
    addIngredient: async (values) => await instance.post('ingredient', values, config()),
    updateIngredient: async (values) => await instance.put(`ingredient/${values.id}`, values, config()),
    deleteIngredient: async (id) => await instance.delete(`ingredient/${id}`, config()),
    findMenu: async (id) => await instance.get(`menu/${id}`, config()),
    listMenus: async () => await instance.get("menu"),
    addMenu: async (values) => await instance.post('menu', values, config()),
    updateMenu: async (values) => await instance.put(`menu/${values.id}`, values, config()),
    deleteMenu: async (id) => await instance.delete(`menu/${id}`, config()),
    assignIngredient: async (id, values) => await instance.put(`menu/${id}/ingredient`, values, config()),
    unAssignIngredient: async (id, ingredientId) => await instance.delete(`menu/${id}/${ingredientId}`, config()),
    readInventory: async (date) => await instance.get(`inventory${date ? '?before=' + date : ""}`, config()),
    addInventory: async (inventory) => await instance.post('inventory', inventory, config()),
    deleteInventory: async (ingredientId) => await instance.delete(`inventory/${ingredientId}`, config()),
    readOrders: async (date) => await instance.get(`order${date ? '?before=' + date : ""}`, config()),
    placeOrder: async (foodOrder) => await instance.post(`/order/${id()}`, foodOrder, config()),
    deleteOrder: async (id) => await instance.delete(`order/${id}`, config()),
    readDeliveries: async (date) => await instance.get(`delivery${date ? '?before=' + date : ""}`, config()),
    readStreets: async () => await instance.get('street', config()),
    restart: async () => await instance.get('inventory/reload', config())
};
