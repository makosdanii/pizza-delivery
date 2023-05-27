const axios = require("axios");
const _ = require("lodash");
const SERVER_URL = "http://localhost:8081";

const instance = axios.create({
    baseURL: SERVER_URL,
    timeout: 3000,
    headers: {Authorization: "root"}
});

let streets = []
const probability = [
    {"0": 0.3},
    {"1": 0.2},
    {"2": 0.1},
    {"3": 0.1},
    {"4": 0.1},
    {"5": 0.2},
    {"6": 0.3},
    {"7": 0.4},
    {"8": 0.5},
    {"9": 0.6},
    {"10": 0.7},
    {"11": 0.8},
    {"12": 0.9},
    {"13": 0.8},
    {"14": 0.7},
    {"15": 0.6},
    {"16": 0.7},
    {"17": 0.8},
    {"18": 0.9},
    {"19": 0.8},
    {"20": 0.7},
    {"21": 0.6},
    {"22": 0.5},
    {"23": 0.4},
]

function findChance(hour) {
    if (hour < 0 || hour > 23) {
        console.log("Error")
        return
    }
    return Object.values(probability
        .find(obj => Object
            .hasOwn(obj, hour.toString())
        ))[0]
}

function getRandomInt(max) {
    return Math.floor(Math.random() * max);
}

// simulation starts at 6 AM
let time = 6 * 60
let wrapper = {
    menuByMenuId: 0,
    userByUserId: 0,
}

instance.get('user/1').then(promise => wrapper.userByUserId = {...promise.data, streetByStreetId: 0})
instance.get('street').then(promise => streets = promise.data)
setInterval(async () => {
    // checks if it's midnight
    if (++time / (24 * 60) >= 1) {
        time = 0
    }
    const hours = Math.floor(time / 60)
    const mins = time % 60
    process.stdout.write(`\r# ${hours < 10 ? '0' : ''}${hours}:${mins < 10 ? '0' : ''}${mins}`);

    const orders = findChance(hours) > Math.random()
    if (orders) {
        let orders = []
        let menus = []
        await instance.get('menu').then(promise => menus = promise.data)

        wrapper.userByUserId.streetNameByStreetNameId = streets[getRandomInt(streets.length)]
        process.stdout.write(` - ${wrapper.userByUserId.streetNameByStreetNameId.that}`)

        do {
            wrapper.menuByMenuId = menus[getRandomInt(menus.length)]
            orders.push(_.cloneDeep(wrapper))

            process.stdout.write(`\n# ${wrapper.menuByMenuId.name}`)
        } while (Math.random() < 0.3) // 30 percent for ordering more

        process.stdout.write("\n# ------Ordered-----> ")
        await instance.post('/order/1', orders).then(promise => console.log(`${promise.data > 0 ? "ACCEPTED" : "REJECTED"}`)).catch(err => console.log(err))
        console.log()
    }
}, 1000)