package com.pizzadelivery.server.services;


import com.pizzadelivery.server.config.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.data.repositories.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class CarService implements ServiceORM<Car> {
    CarRepository carRepository;
    UserRepository userRepository;
    InventoryRepository inventoryRepository;
    IngredientRepository ingredientRepository;
    CarIngredientRepository carIngredientRepository;
    FoodOrderRepository foodOrderRepository;
    OrderDeliveryRepository orderDeliveryRepository;

    @Autowired
    public CarService(CarRepository carRepository,
                      UserRepository userRepository,
                      InventoryRepository inventoryRepository,
                      IngredientRepository ingredientRepository,
                      CarIngredientRepository carIngredientRepository,
                      FoodOrderRepository foodOrderRepository,
                      OrderDeliveryRepository orderDeliveryRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.carIngredientRepository = carIngredientRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.orderDeliveryRepository = orderDeliveryRepository;
    }

    public Car createCar(Car car) throws AlreadyExistsException {
        checkConstraint(car, true);
        car.setId(UNASSIGNED);
        return carRepository.save(car);
    }

    public Optional<Car> findCar(int id) {
        return carRepository.findById(id);
    }

    public Car updateCar(int id, Car car) throws AlreadyExistsException {
        Car old = carRepository.findById(id).orElse(new Car());
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

    public Inventory modifyInventory(Inventory inventory) {
        User user = userRepository
                .findById(((UserAuthorizationDetails) SecurityContextHolder
                        .getContext().getAuthentication().getPrincipal())
                        .getId()).get();
        List<Car> cars = carRepository.findByUserByUserId(user);

        //admin is a good guy so he can borrow car
        if (cars.isEmpty() && user.getRoleByRoleId().getName().equals("admin")) {
            inventory.setCarByCarId(carRepository.findById(1)
                    .orElseThrow(() -> new ConstraintViolationException("No available car", new HashSet<>())));
        } else if (cars.isEmpty()) {
            throw new ConstraintViolationException("Authorized driver has no car", new HashSet<>());
        }

        inventory.setCarByCarId(cars.get(0));

        if (ingredientRepository.existsById(inventory.getIngredientByIngredientId().getId())) {
            int lastQuantity = 0;
            List<Inventory> records;

            //check if any record of the ingredient is present
            if (!(records = inventoryRepository
                    .findByIngredientByIngredientId(inventory.getIngredientByIngredientId(),
                            Sort.by(Sort.Direction.DESC, "id.modifiedAt"))).isEmpty())
                lastQuantity = records.get(0).getCurrentQt();

            //expense is always null if inventory Qt is getting reduced
            if (inventory.getExpense() == 0 && lastQuantity < inventory.getCurrentQt())
                throw new ConstraintViolationException("Calculated current quantity cannot be negative", new HashSet<>());

            inventory.setCurrentQt(inventory.getExpense() > 0 ?
                    lastQuantity + inventory.getCurrentQt() :
                    lastQuantity - inventory.getCurrentQt());

            return inventoryRepository.save(inventory);
        }

        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
    }

    public Car fillIngredient(int id, CarIngredient carIngredient) {
        carIngredient.setCarByCarId(carRepository.findById(id).get());
        if (ingredientRepository.existsById(carIngredient.getIngredientByIngredientId().getId())) {
            Integer lastPercentile = 0;
            List<CarIngredient> records;

            //check if any record of the ingredient is present
            if (!(records = carIngredientRepository
                    .findAllByIdCarByCarIdAndIngredientByIngredientId(
                            carIngredient.getCarByCarId(),
                            carIngredient.getIngredientByIngredientId(),
                            Sort.by(Sort.Direction.DESC, "id.modifiedAt"))).isEmpty())
                lastPercentile = records.get(0).getCurrentPercent();

            if (lastPercentile + carIngredient.getCurrentPercent() > 100)
                throw new ConstraintViolationException("Calculated current percentile cannot be more than 100", new HashSet<>());

            carIngredient.setCurrentPercent(lastPercentile + carIngredient.getCurrentPercent());
            carIngredientRepository.save(carIngredient);

            return carIngredient.getCarByCarId();

        }
        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());

    }

    public Car depleteIngredient(int id, CarIngredient carIngredient) {
        carIngredient.setCarByCarId(carRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid ID constraint", new HashSet<>()))
        );

        if (ingredientRepository.existsById(carIngredient.getIngredientByIngredientId().getId())) {
            Integer lastPercentile = 0;
            List<CarIngredient> records;

            //check if any record of the ingredient is present
            if (!(records = carIngredientRepository
                    .findAllByIdCarByCarIdAndIngredientByIngredientId(
                            carIngredient.getCarByCarId(),
                            carIngredient.getIngredientByIngredientId(),
                            Sort.by(Sort.Direction.DESC, "id.modifiedAt"))).isEmpty())
                lastPercentile = records.get(0).getCurrentPercent();

            if (lastPercentile < carIngredient.getCurrentPercent())
                throw new ConstraintViolationException("Calculated current percentile cannot be negative", new HashSet<>());

            carIngredient.setCurrentPercent(lastPercentile - carIngredient.getCurrentPercent());
            carIngredientRepository.save(carIngredient);

            return carIngredient.getCarByCarId();

        }
        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
    }

    public Car deliverOrder(int id, OrderDelivery orderDelivery) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid ID constraint", new HashSet<>()));

        if (foodOrderRepository.existsById(orderDelivery.getFoodOrderByFoodOrderId().getId())) {
            orderDelivery.setCarByCarId(car);
            orderDeliveryRepository.save(orderDelivery);
            return car;
        }

        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
    }

    @Override
    public void checkConstraint(Car car, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !carRepository.findByLicense(car.getLicense()).isEmpty()) {
            throw new AlreadyExistsException();
        }

        var user = userRepository.findById(car.getUserByUserId().getId());
        if (user.isEmpty() || !user.get().getRoleByRoleId().getName().equals("driver")) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }
}
