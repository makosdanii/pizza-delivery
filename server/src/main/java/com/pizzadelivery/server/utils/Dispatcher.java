package com.pizzadelivery.server.utils;

import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.data.repositories.StreetRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.*;
import com.pizzadelivery.server.utils.callables.Travelling;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Service
public class Dispatcher {
    private final EdgeService edgeService;
    private final InventoryService inventoryService;
    private final MenuService menuService;
    private final CarService carService;
    private final StreetRepository streetRepository;
    private Edge inventoryLocation;
    private final Navigation navigation;
    private final Map<Car, Edge> carLocations = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final int INVENTORY_LOCATION = 10;
    public static final int INVENTORY_CAPACITY = 2000;
    public static final int CAR_CAPACITY = 200;


    @Autowired
    public Dispatcher(EdgeService edgeService,
                      InventoryService inventoryService,
                      CarService carService,
                      MenuService menuService,
                      IngredientService ingredientService,
                      StreetRepository streetRepository) {
        this.streetRepository = streetRepository;
        this.edgeService = edgeService;
        this.inventoryLocation = edgeService.findEdge(INVENTORY_LOCATION);
        navigation = new Navigation(edgeService);

        this.menuService = menuService;
        this.inventoryService = inventoryService;
        executorService.submit(inventoryService::fillInventory);

        this.carService = carService;
        log("Dispatcher is active");
    }

    public int dispatch(List<FoodOrder> foodOrders) throws Exception {
        // API provides edges with the house number in descending order if the edge ids are ascending per street,
        // linearly we search the one which customers house number is on
        Edge orderEdge = findOrderEdge(foodOrders.get(0).getUserByUserId());

        // sort cars idle cars with drivers by their distance to orderEdge cached?
        var availableCars = sortAvailableCar(orderEdge);

        // make union of foodOrders' ingredients then reduce them
        ArrayList<MenuIngredient> sumMenuIngredients = summarizeMenuIngredient(foodOrders);

        // removing any orders which contains any menus for which any ingredients are not supplied that day
        filterForOrdersWithFaultyIngredients(foodOrders, sumMenuIngredients);

        // first car which can deliver,
        // depending on if car's percents cover all the orders' ingredients' quantities, is the best option for closeness
        var eligible = false;
        for (Car car : availableCars) {
            //we need the current carIngredient quantities in the local scope
            final var carIngredients = carService.findCar(car.getId()).orElseThrow().getCarIngredientsById();

            eligible = sumMenuIngredients.stream().allMatch(ingredient -> {
                var carIngredientCorresponding = carIngredients.stream()
                        .filter(carIngredient -> carIngredient.getIngredientByIngredientId()
                                .equals(ingredient.getIngredientByIngredientId()))
                        .findFirst();
                return carIngredientCorresponding.isPresent()
                        && carIngredientCorresponding.get().getCurrentQuantity() >= ingredient.getQuantity();
            });

            if (eligible) {
                sumMenuIngredients.forEach(ingredient -> {
                    int current = carIngredients.stream()
                            .filter(carIngredient -> carIngredient.getIngredientByIngredientId().
                                    equals(ingredient.getIngredientByIngredientId())).findFirst().get()
                            .getCurrentQuantity();

                    carService.modifyCarIngredient(car.getId(),
                            new CarIngredient(current - ingredient.getQuantity(),
                                    ingredient.getIngredientByIngredientId())
                    );
                });

                Callable<Void> deliverOrders = () -> {
                    System.out.printf("Car %d - handing out%n", car.getId());
                    carService.deliverOrders(car.getId(), foodOrders.stream()
                            .map(order -> new OrderDelivery(order, car)).toList());
                    System.out.printf("Car %d - delivery completed%n", car.getId());
                    return null;
                };

                if (navigation.getHistory() == 0)
                    navigation.navigate(car, carLocations.get(car), orderEdge);

                var route = navigation.getRoute(car);
                carLocations.put(car, new Edge());
                executorService.submit(
                        new Travelling(carLocations, navigation.getDistance(car), Arrays.copyOf(route, route.length),
                                car, deliverOrders));
                break;
            } else {
                Callable<Void> fillIngredients = () -> {
                    try {
                        System.out.printf("Car %d - refilling started%n", car.getId());
                        carService.fillCarIngredients(car.getId());
                        System.out.printf("Car %d - refilling completed%n", car.getId());
                    } catch (ConstraintViolationException e) {
                        System.out.printf("Car %d - refilling aborted, depoting%n", car.getId());
                        // if inventory cannot fill a car up anymore, it's better to retire one
                        car.setUserByUserId(null);
                        try {
                            carService.updateCar(car.getId(), car);
                        } catch (AlreadyExistsException ex) {
                            throw new RuntimeException(ex);
                        }
                        // can block the thread
                        carService.depotCarIngredients(car.getId());
                        System.out.printf("Car %d - depot completed, retired%n", car.getId());
                    }
                    return null;
                };

                navigation.navigate(car, carLocations.get(car), inventoryLocation);
                carLocations.put(car, new Edge());
                executorService.submit(new Travelling(carLocations, navigation.getDistance(car),
                        navigation.getRoute(car), car, fillIngredients));
            }
        }

        navigation.clearHistory();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        return eligible ? 1 : 0;
    }

    private void log(String log) {
        System.out.println("----------" + log + "----------");
    }

    private List<Car> sortAvailableCar(Edge finalOrderEdge) throws InterruptedException {
        updateFleetLocation();

        // at start, it's likely that every car is occupied with filling ingredients for a while
        List<Car> availableCars = List.of();
        int sleep = 0;

        do {
            availableCars = carLocations.entrySet().stream()
                    .filter(entry -> entry.getKey().getUserByUserId() != null
                            && entry.getValue().getId() != UNASSIGNED)
                    .toList().stream().sorted(new Comparator<Map.Entry<Car, Edge>>() {
                        @Override
                        public int compare(Map.Entry<Car, Edge> o1, Map.Entry<Car, Edge> o2) {
                            try {
                                return navigation.navigate(o1.getKey(), o1.getValue(), finalOrderEdge)
                                        .compareTo(navigation.navigate(o2.getKey(), o2.getValue(), finalOrderEdge));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }).map(Map.Entry::getKey).toList();
            sleep += 500; // if the reason for availableCars being empty is that there are no drivers, don't wait
        } while (!executorService.awaitTermination(sleep, TimeUnit.MILLISECONDS) && availableCars.isEmpty());
        return availableCars;
    }

    private void updateFleetLocation() {
        // using instances retrieved from carService to catch if new car was registered, or new driver hopped in,
        // in that case it gets filled up
        carService.listAll().forEach(car -> {
            if (!carLocations.containsKey(car)) {
                carLocations.put(car, inventoryLocation);
            } else {
                Edge edge = carLocations.remove(car);
                carLocations.put(car, edge);
            }

            if (car.getUserByUserId() != null && car.getCarIngredientsById().isEmpty()) {
                // although the car does not need to travel for refill, it still needs to be locked
                carLocations.put(car, new Edge());
                executorService.submit(() -> {
                    try {
                        System.out.printf("Car %d - refilling started%n", car.getId());
                        carService.fillCarIngredients(car.getId());
                        System.out.printf("Car %d - refilling completed%n", car.getId());
                        carLocations.put(car, inventoryLocation);
                    } catch (ConstraintViolationException e) {
                        System.out.printf("Car %d - refilling aborted, depoting%n", car.getId());
                        // if inventory cannot fill a car up anymore, it's better to retire one
                        car.setUserByUserId(null);
                        try {
                            carService.updateCar(car.getId(), car);
                        } catch (AlreadyExistsException ex) {
                            throw new RuntimeException(ex);
                        }
                        // can block the thread
                        carService.depotCarIngredients(car.getId());
                        System.out.printf("Car %d - depot completed, retired%n", car.getId());
                    }
                });
            }
        });
    }

    private Edge findOrderEdge(User customer) {
        StreetName street = streetRepository
                .findById(customer.getStreetNameByStreetNameId().getId()).orElseThrow();
        Edge orderEdge = null;

        Edge[] edges = street.getEdgesById().stream().sorted().<Edge>toArray(Edge[]::new);
        for (int i = 0; i < edges.length - 1; i++) {
            if (edges[i].getVertex() >= customer.getHouseNo() &&
                    customer.getHouseNo() >= edges[i + 1].getVertex())
                orderEdge = edges[i];
        }

        if (orderEdge == null) orderEdge = edges[edges.length - 1];

        return orderEdge;
    }

    private ArrayList<MenuIngredient> summarizeMenuIngredient(List<FoodOrder> foodOrders) {
        List<MenuIngredient> allMenuIngredients = foodOrders.stream().flatMap(order -> menuService
                .listAllMenuIngredientsByIds(order.getMenuByMenuId().getId())).toList();

        ArrayList<MenuIngredient> sumMenuIngredients = new ArrayList<>();
        // menuIngredient summarized qt is set when on the ingredient's first appearance in allMenuIngredient,
        // later it's ignored when already in sumIngredients
        allMenuIngredients.forEach(menuIngredient -> {
            var alreadyContainedIngredient = sumMenuIngredients.stream().filter(ingredient -> ingredient
                    .getIngredientByIngredientId().equals(menuIngredient.getIngredientByIngredientId())).findFirst();
            if (alreadyContainedIngredient.isEmpty()) {
                menuIngredient.setQuantity(allMenuIngredients.stream()
                        .filter(ingredient -> ingredient.getIngredientByIngredientId()
                                .equals(menuIngredient.getIngredientByIngredientId()))
                        .reduce(0, (acc, curr) -> acc + curr.getQuantity(), Integer::sum));
                sumMenuIngredients.add(menuIngredient);
            }
        });

        return sumMenuIngredients;
    }

    private void filterForOrdersWithFaultyIngredients(List<FoodOrder> foodOrders, List<MenuIngredient> sumMenuIngredients) {
        for (MenuIngredient sumMenuIngredient : sumMenuIngredients) {
            if (inventoryService.readInventoryLatestQt(sumMenuIngredient.getIngredientByIngredientId())
                    < sumMenuIngredient.getQuantity()) {
                foodOrders = foodOrders.stream()
                        .filter(order -> {
                            var orderIngredients = order.getMenuByMenuId().getMenuIngredientsById().stream();
                            var orderFaultyIngredient = orderIngredients.filter(_ingredient -> _ingredient
                                    .getIngredientByIngredientId()
                                    .equals(sumMenuIngredient.getIngredientByIngredientId())).findFirst();
                            return orderFaultyIngredient.isEmpty();
                        }).toList();
            }
        }
    }

    public void shutDown() {
        boolean termination = false;
        log("Termination has begun");
        try {
            termination = executorService.awaitTermination(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        if (termination) {
            log("Depoting");
            carLocations.keySet().forEach(car -> {
                executorService.submit(() -> carService.depotCarIngredients(car.getId()));
                car.setUserByUserId(null);
                try {
                    carService.updateCar(car.getId(), car);
                } catch (AlreadyExistsException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        log("Finished");
    }
}
