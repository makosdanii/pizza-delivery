package com.pizzadelivery.server.services;


import com.pizzadelivery.server.config.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.CarIngredient;
import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.repositories.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.pizzadelivery.server.utils.Dispatcher.CAR_CAPACITY;
import static java.lang.Thread.sleep;

@Service
public class CarService implements ServiceORM<Car> {
    CarRepository carRepository;
    UserRepository userRepository;
    InventoryRepository inventoryRepository;
    IngredientRepository ingredientRepository;
    CarIngredientRepository carIngredientRepository;
    FoodOrderRepository foodOrderRepository;
    OrderDeliveryRepository orderDeliveryRepository;
    InventoryService inventoryService;

    @Autowired
    public CarService(CarRepository carRepository,
                      UserRepository userRepository,
                      InventoryRepository inventoryRepository,
                      IngredientRepository ingredientRepository,
                      CarIngredientRepository carIngredientRepository,
                      FoodOrderRepository foodOrderRepository,
                      OrderDeliveryRepository orderDeliveryRepository,
                      InventoryService inventoryService) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.carIngredientRepository = carIngredientRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.orderDeliveryRepository = orderDeliveryRepository;
        this.inventoryService = inventoryService;
    }

    public Car createCar(Car car) throws AlreadyExistsException {
        checkConstraint(car, true);
        car.setId(UNASSIGNED);
        return carRepository.save(car);
    }

    public Optional<Car> findCar(int id) {
        return carRepository.findById(id);
    }

    public Iterable<Car> listAll() {
        return carRepository.findAll();
    }

    public Car updateCar(int id, Car car) throws AlreadyExistsException {
        Car old = carRepository.findById(id).orElse(new Car());
        var authenticated = Optional.ofNullable(SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal());

        if (!authenticated.isEmpty()) {
            // only admin can update license plate
            if (!((UserAuthorizationDetails) authenticated.get()).getAuthorities()
                    .contains(new SimpleGrantedAuthority("admin")))
                car.setLicense(old.getLicense());

            if (old.getUserByUserId() != null && !isAuthorizedForCar(old) ||
                    car.getUserByUserId() != null && !isAuthorizedForCar(car))
                throw new ConstraintViolationException("Only admin or driver can assign people", new HashSet<>());
        }

        if (old.getId() != UNASSIGNED) {
            checkConstraint(car, !old.getLicense().equals(car.getLicense()));
            car.setId(id);
            return carRepository.save(car);
        }

        return old;
    }

    public boolean deleteCar(int id) {
        if (!carRepository.existsById(id)) {
            return false;
        }

        carRepository.deleteById(id);
        return true;
    }

    public void modifyCarIngredient(int id, CarIngredient carIngredient) {
        carIngredient.setCarByCarId(carRepository.findById(id).get());
        if (!ingredientRepository.existsById(carIngredient.getIngredientByIngredientId().getId())) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        } else {
            carIngredientRepository.save(carIngredient);
            carRepository.findById(id).get();
        }

    }

    public Car fillCarIngredients(int carId) {
        var car = carRepository.findById(carId).orElseThrow();
        ingredientRepository.findAll().forEach(ingredient -> {
            var corresponding = car.getCarIngredientsById().stream()
                    .filter(carIngredient -> carIngredient.getIngredientByIngredientId().equals(ingredient)).findFirst()
                    .orElse(new CarIngredient(0, ingredient, car));

            int deficit = CAR_CAPACITY - corresponding.getCurrentQuantity();
            if (deficit > 0) {
                inventoryService.modifyInventory(new Inventory(car, 0,
                        deficit, ingredient), false);
                corresponding.setCurrentQuantity(CAR_CAPACITY);
                carIngredientRepository.save(corresponding);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return car;
    }

    public void depotCarIngredients(int carId) {
        carRepository.findById(carId).orElseThrow().getCarIngredientsById().forEach(carIngredient -> {
            if (carIngredient.getCurrentQuantity() > 0) {
                inventoryService.modifyInventory(new Inventory(carIngredient.getCarByCarId(), 0,
                        carIngredient.getCurrentQuantity(), carIngredient.getIngredientByIngredientId()), true);
                carIngredient.setCurrentQuantity(0);
                carIngredientRepository.save(carIngredient);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public Car deliverOrders(int id, List<OrderDelivery> orderDeliveries) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid ID constraint", new HashSet<>()));
        orderDeliveries.forEach(orderDelivery -> {
            if (!foodOrderRepository.existsById(orderDelivery.getFoodOrderByFoodOrderId().getId())) {
                throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
            } else {
                orderDelivery.setCarByCarId(car);
                orderDeliveryRepository.save(orderDelivery);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
        return car;
    }

    @Override
    public void checkConstraint(Car car, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !carRepository.findByLicense(car.getLicense()).isEmpty()) {
            throw new AlreadyExistsException();
        }

        if (car.getUserByUserId() == null) return;

        var user = userRepository.findById(car.getUserByUserId().getId());

        if (!user.isPresent() || !user.get().getRoleByRoleId().getName().equals("driver")) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }

    private boolean isAuthorizedForCar(Car car) {
        UserAuthorizationDetails authenticated = (UserAuthorizationDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return authenticated.getAuthorities().contains(new SimpleGrantedAuthority("admin"))
                || car.getUserByUserId().getId() == authenticated.getId();
    }

}
