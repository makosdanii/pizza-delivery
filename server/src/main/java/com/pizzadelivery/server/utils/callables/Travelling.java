package com.pizzadelivery.server.utils.callables;

import com.pizzadelivery.server.controllers.websocket.ChatController;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.Edge;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.services.CarService;
import com.pizzadelivery.server.services.InventoryService;
import com.pizzadelivery.server.utils.Navigation;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.pizzadelivery.server.utils.Dispatcher.INVENTORY_LOCATION;
import static java.lang.Thread.sleep;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Travelling implements Callable<String> {
    private final ChatController chatController;
    private final CarService carService;
    private final InventoryService inventoryService;
    private Navigation navigation;
    private List<FoodOrder> foodOrders;
    private Map<Car, Edge> map;
    private Car car;
    private Function<Car, Integer> depot;

    public Travelling(ChatController chatController, CarService carService, InventoryService inventoryService) {
        this.chatController = chatController;
        this.carService = carService;
        this.inventoryService = inventoryService;
    }

    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
    }

    public void setFoodOrders(List<FoodOrder> foodOrders) {
        this.foodOrders = foodOrders;
    }

    public void setMap(Map<Car, Edge> map) {
        this.map = map;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setDepot(Function<Car, Integer> depot) {
        this.depot = depot;
    }

    @Override
    public String call() {
        try {
            var route = navigation.getRoute(car);
            var distance = navigation.getDistance(car);
            var delivering = !foodOrders.isEmpty();
            var id = delivering ? foodOrders.get(0).getUserByUserId().getId() : 0;

            System.out.printf("Car %d - setting off for a %d long route%n",
                    car.getId(), distance,
                    route[route.length - 1].getId() == INVENTORY_LOCATION ? "refilling" : "delivering");

            //travel speed: meter/millisecond
            Arrays.stream(route).forEach(street -> {
                System.out.printf("Car %d - at %s%n", car.getId(), street.getStreetNameByEdgeName().getThat());

                if (delivering)
                    chatController.send(id, street.getStreetNameByEdgeName().getThat());

                try {
                    sleep(street.getEdgeWeight());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            if (delivering) {
                chatController.send(id, "Arrived with the followings:");
                System.out.printf("Car %d - arrived%n", car.getId());
                foodOrders.forEach(order -> {
                    carService.persist(new OrderDelivery(order, car));
                    chatController.send(id, order.getMenuByMenuId().getName());
                    System.out.printf("Car %d - delivered a %s%n", car.getId(), order.getMenuByMenuId().getName());
                });
            } else {
                carService.fillCarIngredients(car).forEach(((carIngredient, inventory) -> {
                    // it's the same function tbh
                    carService.persist(carIngredient);
                    inventoryService.persist(inventory);
                }));
                System.out.printf("Car %d - refilled, ready%n", car.getId());
            }

            map.put(car, route[route.length - 1]);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return "Finished";
    }
}
