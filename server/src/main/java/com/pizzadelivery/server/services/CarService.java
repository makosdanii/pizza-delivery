package com.pizzadelivery.server.services;

import com.pizzadelivery.server.config.utils.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.data.repositories.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.*;

import static com.pizzadelivery.server.utils.Dispatcher.CAR_CAPACITY;

@Service
public class CarService extends ServiceORM<Car> {
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

    public Car findCar(int id) {
        return carRepository.findById(id).orElse(new Car());
    }

    public Iterable<Car> listAll(boolean isAdmin) {
        if (isAdmin) {
            return carRepository.findAll();
        } else {
            UserAuthorizationDetails driver = (UserAuthorizationDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return carRepository.findAllByUserByUserIdOrUserByUserIdIsNull(userRepository.findById(driver.getId()).get());
        }
    }

    // persist return value in transactional
    public Car updateCar(int id, Car car) throws AlreadyExistsException {
        Car old = carRepository.findById(id).orElse(new Car());

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            // only admin can update license plate
            if (!SecurityContextHolder.getContext()
                    .getAuthentication().getAuthorities()
                    .contains(new SimpleGrantedAuthority("admin")))
                car.setLicense(old.getLicense());

            // case of leaving the car
            if (old.getUserByUserId() != null && NotAuthorizedForCar(old))
                throw new ConstraintViolationException("Only admin or driver can assign people", new HashSet<>());

            // case of driving it
            if (car.getUserByUserId() != null && NotAuthorizedForCar(car))
                throw new ConstraintViolationException("Only admin or driver can assign people", new HashSet<>());

        }

        if (old.getId() != UNASSIGNED) {
            checkConstraint(car, !old.getLicense().equals(car.getLicense()));
            car.setId(id);

            if (car.getUserByUserId() != null) {
                User driver = userRepository.findById(car.getUserByUserId().getId()).orElse(new User());
                Car assignedCar = driver.getCarByCarId();
                if (assignedCar != null && assignedCar != car) {
                    throw new ConstraintViolationException("Driver is already assigned to another car", new HashSet<>());
                }
            }
            return car;
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

    public List<CarIngredient> listCarIngredients(Car car) {
        return carIngredientRepository.findAllByIdCarByCarId(car);
    }

    public int showCarFuelLevel(Car car) {
        Ingredient fuel;
        try {
            fuel = ingredientRepository.findByName("fuel").get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new ConstraintViolationException("Necessary ingredient is yet to be created", new HashSet<>());
        }
        CarIngredient fuelLevel = carIngredientRepository.findAllByIdCarByCarId(car).stream()
                .filter(carIngredient -> carIngredient.getIngredientByIngredientId().equals(fuel))
                .findFirst()
                .orElse(new CarIngredient(0, fuel, car));
        return fuelLevel.getCurrentQuantity();
    }


    /**
     * @param car of which ingredients are filled
     * @return map of deficient {@code carIngredients} and corresponding inventory decreases
     */
    public Map<CarIngredient, Inventory> fillCarIngredients(Car car) {
        Map<CarIngredient, Inventory> ingredients = new HashMap<>();

        ingredientRepository.findAll().forEach(ingredient -> {
            CarIngredient corresponding = listCarIngredients(car).stream()
                    .filter(carIngredient -> carIngredient.getIngredientByIngredientId().equals(ingredient)).findFirst()
                    .orElse(new CarIngredient(0, ingredient, car));

            int deficit = CAR_CAPACITY - corresponding.getCurrentQuantity();
            if (deficit > 0) {
                try {
                    Inventory inventoryChange = null;
                    if (!Objects.equals(corresponding.getIngredientByIngredientId().getName(), "fuel"))
                        inventoryChange = inventoryService.modifyInventory(new Inventory(car, 0,
                                deficit, ingredient), false);

                    corresponding.setCurrentQuantity(CAR_CAPACITY);
                    ingredients.put(corresponding, inventoryChange);
                } catch (ConstraintViolationException e) {
                    System.out.printf("Ingredient %s - Requested quantity (%d) unavailable%n", ingredient.getName(), deficit);
                }
            }
        });
        return ingredients;
    }

    /**
     * @param car of which leftovers are depoted
     * @return list of records of inventories increased with leftovers to be persisted
     */
    public List<Inventory> depotCarIngredients(Car car) {
        List<Inventory> ingredients = new ArrayList<>();
        listCarIngredients(car).forEach(carIngredient -> {
            if (carIngredient.getCurrentQuantity() > 0) {
                Inventory inventoryChange = inventoryService.modifyInventory(new Inventory(carIngredient.getCarByCarId(), 0,
                        carIngredient.getCurrentQuantity(), carIngredient.getIngredientByIngredientId()), true);
                ingredients.add(inventoryChange);
            }
        });

        return ingredients;
    }

    @Transactional
    public void dropCarIngredients(Car car) {
        carIngredientRepository.deleteAllByIdCarByCarId(car);
    }

    @Override
    public void checkConstraint(Car car, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !carRepository.findByLicense(car.getLicense()).isEmpty()) {
            throw new AlreadyExistsException();
        }

        if (car.getUserByUserId() == null) return;

        Optional<User> user = userRepository.findById(car.getUserByUserId().getId());

        if (!user.isPresent() || !user.get().getRoleByRoleId().getName().equals("driver")) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }

    /**
     * @param car to be checked
     * @return whether user is neither admin nor the one previously assigned to the car
     */
    private boolean NotAuthorizedForCar(Car car) {
        UserAuthorizationDetails authenticated = (UserAuthorizationDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return !authenticated.getAuthorities().contains(new SimpleGrantedAuthority("admin"))
                && car.getUserByUserId().getId() != authenticated.getId();
    }

    @Transactional
    public void saveCarInTransaction(Car car) {
        carRepository.save(car);
    }

    @Transactional
    public void saveIngredientInTransaction(CarIngredient carIngredient) {
        carIngredientRepository.save(carIngredient);
    }

    public void saveDelivery(OrderDelivery orderDelivery) {
        orderDeliveryRepository.save(orderDelivery);
    }
}
