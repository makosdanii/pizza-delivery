package com.pizzadelivery.server.utils.callables;

import com.pizzadelivery.server.controllers.websocket.ChatController;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.Edge;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.services.CarService;
import com.pizzadelivery.server.utils.Navigation;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.pizzadelivery.server.utils.Dispatcher.INVENTORY_LOCATION;
import static java.lang.Thread.sleep;

/**
 * Class implements {@code Callable}. In the method, which is overridden, the thread is slept for time periods
 * which basically resembles the travelling process. It also logs the journey to either a customer or the inventory.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Travelling implements Callable<String> {
    private final ChatController chatController;
    private final CarService carService;
    private Navigation navigation;
    private List<FoodOrder> foodOrders;
    private Map<Car, Edge> map;
    private Car car;

    public Travelling(ChatController chatController, CarService carService) {
        this.chatController = chatController;
        this.carService = carService;
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

    @Override
    public String call() {
        try {
            Edge[] route = navigation.getRoute(car);
            int distance = navigation.getDistance(car);
            boolean delivering = !foodOrders.isEmpty();
            int id = delivering ? foodOrders.get(0).getUserByUserId().getId() : 0;

            System.out.printf("Car %d - Setting off for a %d long route to %s.%n",
                    car.getId(), distance,
                    route[route.length - 1].getId() == INVENTORY_LOCATION ? "refill" : "deliver");

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
                    // order simulation runs under admin privilege, no need to store those
                    if (!Objects.equals(order.getUserByUserId().getRoleByRoleId().getName(), "admin"))
                        carService.saveDelivery(new OrderDelivery(order, car));
                    chatController.send(id, order.getMenuByMenuId().getName());
                    System.out.printf("Car %d - Delivered a %s.%n", car.getId(), order.getMenuByMenuId().getName());
                });
            } else {
                System.out.printf("Car %d - Back at inventory, ready.%n", car.getId());
            }

            map.put(car, route[route.length - 1]);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return "Finished";
    }
}
