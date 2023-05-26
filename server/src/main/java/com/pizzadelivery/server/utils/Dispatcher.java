package com.pizzadelivery.server.utils;

import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.data.repositories.StreetRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.CarService;
import com.pizzadelivery.server.services.EdgeService;
import com.pizzadelivery.server.services.InventoryService;
import com.pizzadelivery.server.services.MenuService;
import com.pizzadelivery.server.utils.callables.Travelling;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;
import static java.lang.Thread.sleep;

/**
 * Service responsible for handling deliveries for any food order.
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Dispatcher {
    private final ApplicationContext applicationContext;
    private final InventoryService inventoryService;
    private final MenuService menuService;
    private final CarService carService;
    private final StreetRepository streetRepository;
    private final Edge inventoryLocation;
    private final Navigation navigation;
    private final Map<Car, Edge> carLocations = new HashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static final int INVENTORY_LOCATION = 10;
    public static final int INVENTORY_CAPACITY = 2000;
    public static final int CAR_CAPACITY = 200;

    @Autowired
    public Dispatcher(ApplicationContext applicationContext,
                      EdgeService edgeService,
                      InventoryService inventoryService,
                      CarService carService,
                      MenuService menuService,
                      StreetRepository streetRepository) {
        this.applicationContext = applicationContext;
        this.streetRepository = streetRepository;
        this.inventoryLocation = edgeService.findEdge(INVENTORY_LOCATION);
        navigation = new Navigation(edgeService);
        this.menuService = menuService;
        this.inventoryService = inventoryService;
        this.carService = carService;
    }

    /**
     * Fills up the inventory with the deficit of its maximum capacity.
     */
    @PostConstruct
    private void initialize() {
        inventoryService.fillInventory().forEach(inventory -> {
            if (inventory != null) inventoryService.persist(inventory);
        });
        log("Inventory filled");
    }

    /**
     * Shuts down its {@code executorService} and calls depot for each car with ingredients
     */
    @PreDestroy
    private void shutDown() {
        log("Termination started");

        boolean shutDown = false;
        try {
            executorService.shutdown();
            shutDown = executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        if (shutDown) {
            Set<Car> cars = new HashSet<>(carLocations.keySet());
            cars.forEach(this::depot);
        }
    }

    /**
     * responsible for conducting the delivery process
     *
     * @param foodOrders orders to handle
     * @return number of successful deliveries
     */
    public int dispatch(List<FoodOrder> foodOrders) {
        Edge orderEdge = findOrderEdge(foodOrders.get(0).getUserByUserId());

        List<Car> availableCars = sortAvailableCars(orderEdge);

        List<MenuIngredient> sumMenuIngredients = summarizeMenuIngredient(foodOrders);

        filterForOrdersWithFaultyIngredients(foodOrders, sumMenuIngredients);
        if (foodOrders.isEmpty())
            return 0;

        // first car which can deliver,
        // depending on if car's percents cover all the orders' ingredients' quantities, is the best option for closeness
        boolean eligible = false;
        for (Car car : availableCars) {
            // we need the current carIngredient quantities in the local scope
            final List<CarIngredient> carIngredients = carService.listCarIngredients(car);

            // check if it has all the ingredient amounts which needed to bake onboard
            eligible = sumMenuIngredients.stream().allMatch(ingredient -> {
                Optional<CarIngredient> carIngredientCorresponding = carIngredients.stream()
                        .filter(carIngredient -> carIngredient.getIngredientByIngredientId()
                                .equals(ingredient.getIngredientByIngredientId()))
                        .findFirst();
                return carIngredientCorresponding.isPresent()
                        && carIngredientCorresponding.get().getCurrentQuantity() >= ingredient.getQuantity();
            });

            if (eligible) {
                // reduce from car's ingredients
                sumMenuIngredients.forEach(ingredient -> {
                    int current = carIngredients.stream()
                            .filter(carIngredient -> carIngredient.getIngredientByIngredientId().
                                    equals(ingredient.getIngredientByIngredientId())).findFirst().get()
                            .getCurrentQuantity();

                    carService.persist(new CarIngredient(current - ingredient.getQuantity(),
                            ingredient.getIngredientByIngredientId(), car));
                });

                setOff(car, orderEdge, foodOrders);
                break;
            } else {
                carService.fillCarIngredients(car).forEach(((carIngredient, inventory) -> {
                    // it's the same function tbh
                    carService.persist(carIngredient);
                    inventoryService.persist(inventory);
                }));

                // empty list means that car cannot deliver the orders so needs to get refilled
                setOff(car, inventoryLocation, List.of());
            }
        }

        return eligible ? foodOrders.size() : 0;
    }

    private void log(String log) {
        int length = log.length();
        if (length / 2 + 1 > 25) {
            System.out.println(log);
            return;
        }
        String prefix;
        String postfix;
        if (length % 2 == 0) {
            prefix = "_".repeat(25 - length / 2);
            postfix = "_".repeat(25 - length / 2);
        } else {
            prefix = "_".repeat(25 - length / 2);
            postfix = "_".repeat(25 - length / 2 + 1);
        }
        System.out.println(prefix + log + postfix);
    }

    /**
     * prepares a task for simulating the calculated route
     *
     * @param car         which will travel, meaning altered location in {@code carLocation}
     * @param destination edge necessary to calculate route
     * @param foodOrders  just for being able to log the delivery
     */
    private void setOff(Car car, Edge destination, List<FoodOrder> foodOrders) {
        navigation.navigate(car, carLocations.get(car), destination);
        Travelling task = applicationContext.getBean(Travelling.class);
        task.setNavigation(navigation);
        task.setCar(car);
        task.setMap(carLocations);
        task.setFoodOrders(foodOrders);

        // means locking the car from further dispatching until task is finished
        carLocations.put(car, new Edge());
        executorService.submit(task);
    }

    /**
     * first updates, then sorts the {@code carLocations} list by the proximity to target     *
     *
     * @param finalOrderEdge the target
     * @return sorted list
     */
    private List<Car> sortAvailableCars(Edge finalOrderEdge) {
        updateFleetLocation();

        List<Map.Entry<Car, Edge>> availableCars = carLocations.entrySet().stream()
                .filter(entry -> entry.getValue().getId() != UNASSIGNED).toList();

        if (availableCars.isEmpty()) {
            log("Car fleet empty");
            return List.of();
        }

        return availableCars.stream().sorted((o1, o2) -> {
            try {
                return navigation.navigate(o1.getKey(), o1.getValue(), finalOrderEdge)
                        .compareTo(navigation.navigate(o2.getKey(), o2.getValue(), finalOrderEdge));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).map(Map.Entry::getKey).toList();
    }

    /**
     * Iterates through the instances retrieved from {@code carService} to catch if new driver hopped in,
     * in that case it gets filled up. Also, depots abandoned cars.
     */
    private void updateFleetLocation() {
        carService.listAll(true).forEach(car -> {
            // doesn't care if userId == null and haven't been put into map
            if (car.getUserByUserId() == null && carLocations.containsKey(car)) {
                System.out.printf("Car %d - drive left%n", car.getId());

                depot(car);
                // doesn't care if userId != null and already in the map
            } else if (car.getUserByUserId() != null && !carLocations.containsKey(car)) {
                System.out.printf("Car %d - driver arrived%n", car.getId());
                carService.fillCarIngredients(car).forEach(((carIngredient, inventory) -> {
                    // it's the same function tbh
                    carService.persist(carIngredient);
                    inventoryService.persist(inventory);
                }));

                carLocations.put(car, inventoryLocation);
                System.out.printf("Car %d - filled, ready%n", car.getId());
            }
        });
    }

    /**
     * tries to locate which is the proper {@code edge} of the numerous ones belonging to one street,
     * which hosts the {@code houseNo}
     *
     * @param customer the object in which the address details are wrapped
     * @return the located edge
     */
    private Edge findOrderEdge(User customer) {
        StreetName street = streetRepository
                .findById(customer.getStreetNameByStreetNameId().getId()).orElseThrow();
        Edge orderEdge = null;

        // API provides edges with the house number in descending order if the edge ids are ascending per street,
        // linearly we search the one which customers house number is on
        Edge[] edges = street.getEdgesById().stream().sorted().toArray(Edge[]::new);
        for (int i = 0; i < edges.length - 1; i++) {
            if (edges[i].getVertex() >= customer.getHouseNo() &&
                    customer.getHouseNo() >= edges[i + 1].getVertex())
                orderEdge = edges[i];
        }

        if (orderEdge == null) orderEdge = edges[edges.length - 1];

        return orderEdge;
    }

    /**
     * summarizes by making union of foodOrders' ingredients and setting their quantities to accumulated value
     *
     * @param foodOrders to be summarized
     * @return list of summarized values
     */
    private List<MenuIngredient> summarizeMenuIngredient(List<FoodOrder> foodOrders) {
        List<MenuIngredient> allMenuIngredients = foodOrders.stream().flatMap(order -> menuService
                .listAllMenuIngredientsByIds(order.getMenuByMenuId().getId())).toList();

        List<MenuIngredient> sumMenuIngredients = new ArrayList<>();
        // menuIngredient summarized qt is set when on the ingredient's first appearance in allMenuIngredient,
        // later it's ignored when already in sumIngredients
        allMenuIngredients.forEach(menuIngredient -> {
            Optional<MenuIngredient> alreadyContainedIngredient = sumMenuIngredients.stream().filter(ingredient -> ingredient
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

    /**
     * Removes any orders which contains any menus for which any ingredients are not supplied that day
     * or inventory is out of.
     *
     * @param foodOrders         to be filtered
     * @param sumMenuIngredients the basis of filtering
     */
    private void filterForOrdersWithFaultyIngredients(List<FoodOrder> foodOrders, List<MenuIngredient> sumMenuIngredients) {
        List faulty = new ArrayList<MenuIngredient>();
        for (MenuIngredient sumMenuIngredient : sumMenuIngredients) {
            if (inventoryService.readInventoryLatestQt(sumMenuIngredient.getIngredientByIngredientId())
                    < sumMenuIngredient.getQuantity()) {
                // remove any food order which has the problematic ingredient
                foodOrders.removeAll(foodOrders.stream()
                        .filter(order -> {
                            Stream<MenuIngredient> orderIngredients = order.getMenuByMenuId().getMenuIngredientsById().stream();
                            Optional<MenuIngredient> orderFaultyIngredient = orderIngredients.filter(_ingredient -> _ingredient
                                    .getIngredientByIngredientId()
                                    .equals(sumMenuIngredient.getIngredientByIngredientId())).findFirst();
                            return orderFaultyIngredient.isPresent();
                        }).toList());
                faulty.add(sumMenuIngredient);
                System.out.printf("Menu %s - rejected, %s is unavailable in inventory%n",
                        sumMenuIngredient.getMenuByMenuId().getName(),
                        sumMenuIngredient.getIngredientByIngredientId().getName());
            }
        }
        // finally any problematic order is removed so their ingredients should be removed as well
        sumMenuIngredients.removeAll(faulty);
    }

    /**
     * wraps method {@code shutDown} and {@code initialize} into one function, also reinitialized {@code executorService}
     *
     * @return success
     */
    public int reset() {
        shutDown();
        try {
            log("Terminated - Reloading");
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        initialize();
        log("Dispatcher is active");
        return 0;
    }

    /**
     * removes the car from memory stored {@code carLocations}, detaches the driver,
     * and removes the leftover ingredients in the car, filling the inventory up with them
     *
     * @param car to be depoted
     */
    private void depot(Car car) {
        // if inventory cannot fill a car up anymore, it's better to retire one
        carLocations.remove(car);
        car.setUserByUserId(null);
        try {
            Car entity = carService.updateCar(car.getId(), car);
            carService.persist(entity);
        } catch (AlreadyExistsException ex) {
            throw new RuntimeException(ex);
        }

        carService.depotCarIngredients(car).forEach(inventory -> {
            // it's the same function tbh
            carService.dropCarIngredients(car);
            inventoryService.persist(inventory);
        });

        System.out.printf("Car %d - depoted, retired%n", car.getId());
    }
}
