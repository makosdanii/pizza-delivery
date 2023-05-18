import * as FileSaver from "file-saver";
import * as XLSX from "xlsx";
import server from '@/business/PizzaServerAPI'

const fileExtension = ".xlsx"
export default {
    exportToSpreadsheet: async (date) => {
        let fileName = date
        let inventory = []
        let order = []
        let delivery = []

        await server.readInventory(date.toISOString().split('T')[0]).then(promise => {
            inventory = promise.data
                .map(record => {
                    const car = record.carByCarId;
                    const ingredient = record.ingredientByIngredientId;
                    delete record.carByCarId
                    delete record.ingredientByIngredientId
                    return {...record, ingredient: ingredient.name, car: car.license}
                })
            console.log("finished")
        })

        if (inventory.length) {
            console.log("hit")
            fileName = new Date(inventory[inventory.length - 1].modifiedAt)
        }

        await server.readOrders(date.toISOString().split('T')[0]).then(promise => {
            order = promise.data
                .map(record => {
                    const menu = record.menuByMenuId;
                    const user = record.userByUserId;
                    delete record.menuByMenuId
                    delete record.userByUserId
                    return {...record, menu: menu.name, user: user.email}
                })
        })

        if (order.length && new Date(order[order.length - 1].orderedAt) > fileName) {
            fileName = new Date([order.length - 1].orderedAt)
        }

        await server.readDeliveries(date.toISOString().split('T')[0]).then(promise => {
            delivery = promise.data
                .map(record => {
                    const car = record.carByCarId;
                    const order = record.foodOrderByFoodOrderId;
                    delete record.carByCarId
                    delete record.foodOrderByFoodOrderId
                    return {...record, order: order.id, car: car.license}
                })
        })

        if (delivery.length && new Date(delivery[delivery.length - 1].deliveredAt) > fileName) {
            fileName = new Date([delivery.length - 1].deliveredAt)
        }

        const inventorySheet = XLSX.utils.json_to_sheet(inventory);
        const orderSheet = XLSX.utils.json_to_sheet(order);
        const deliverySheet = XLSX.utils.json_to_sheet(delivery);

        const workBook = {
            Sheets: {Inventory: inventorySheet, Orders: orderSheet, Deliveries: deliverySheet},
            SheetNames: ["Inventory", "Orders", "Deliveries"],
        };

        fileName = fileName.toISOString().split('T')[0]
        // Exporting the file with the desired name and extension.
        XLSX.writeFileXLSX(workBook, fileName + fileExtension);
    }
};