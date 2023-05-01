package com.pizzadelivery.server.utils.callables;

import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.Edge;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.pizzadelivery.server.utils.Dispatcher.INVENTORY_LOCATION;
import static java.lang.Thread.sleep;


public class Travelling implements Callable<String> {
    private final Map<Car, Edge> map;
    private final int distance;
    private final Edge[] route;
    private final Car car;
    private final Callable<Void> callable;

    public Travelling(Map<Car, Edge> map, int distance, Edge[] route, Car car, Callable<Void> callable) {
        this.map = map;
        this.distance = distance;
        this.route = route;
        this.car = car;
        this.callable = callable;
    }

    @Override
    public String call() throws Exception {
        System.out.printf("Car %d - setting off for a %d long route, %s%n",
                car.getId(), distance,
                route[route.length - 1].getId() == INVENTORY_LOCATION ? "refilling" : "delivering");

        //travel speed: meter/millisecond
        Arrays.stream(route).forEach(street -> {
            System.out.printf("Car %d - at %s%n", car.getId(), street.getStreetNameByEdgeName().getThat());
            try {
                sleep(street.getEdgeWeight());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.printf("Car %d - arrived%n", car.getId());
        callable.call();
        map.put(car, route[route.length - 1]);
        return "Finished";
    }

}
