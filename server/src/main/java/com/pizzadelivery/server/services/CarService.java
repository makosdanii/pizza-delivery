package com.pizzadelivery.server.services;

import com.pizzadelivery.server.config.utils.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.CarIngredient;
import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.repositories.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Optional<Car> findCar(int id) {
        return carRepository.findById(id);
    }

    public Iterable<Car> listAll(boolean isAdmin) {
        if (isAdmin) {
            return carRepository.findAll();
        } else {
            var driver = (UserAuthorizationDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return carRepository.findAllByUserByUserIdOrUserByUserIdIsNull(userRepository.findById(driver.getId()).get());
        }
    }

    // persist after in transactional
    public Car updateCar(int id, Car car) throws AlreadyExistsException {
        Car old = carRepository.findById(id).orElse(new Car());
        var authenticated = SecurityContextHolder.getContext()
                .getAuthentication().isAuthenticated();

        if (authenticated) {
            // only admin can update license plate
            if (!SecurityContextHolder.getContext()
                    .getAuthentication().getAuthorities()
                    .contains(new SimpleGrantedAuthority("admin")))
                car.setLicense(old.getLicense());

            // case of leaving the car
            if (old.getUserByUserId() != null && !isAuthorizedForCar(old))
                throw new ConstraintViolationException("Only admin or driver can assign people", new HashSet<>());

            // case of driving it
            if (car.getUserByUserId() != null && !isAuthorizedForCar(car))
                throw new ConstraintViolationException("Only admin or driver can assign people", new HashSet<>());
        }

        if (old.getId() != UNASSIGNED) {
            checkConstraint(car, !old.getLicense().equals(car.getLicense()));
            car.setId(id);
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

    // persist after in transactional
    public Map<CarIngredient, Inventory> fillCarIngredients(Car car) {
        Map<CarIngredient, Inventory> ingredients = new HashMap<>();

        ingredientRepository.findAll().forEach(ingredient -> {
            CarIngredient corresponding = listCarIngredients(car).stream()
                    .filter(carIngredient -> carIngredient.getIngredientByIngredientId().equals(ingredient)).findFirst()
                    .orElse(new CarIngredient(0, ingredient, car));

            int deficit = CAR_CAPACITY - corresponding.getCurrentQuantity();
            if (deficit > 0) {
                try {
                    Inventory inventoryChange = inventoryService.modifyInventory(new Inventory(car, 0,
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

    // persist after in transactional
    public Map<CarIngredient, Inventory> depotCarIngredients(Car car) {
        Map<CarIngredient, Inventory> ingredients = new HashMap<>();
        listCarIngredients(car).forEach(carIngredient -> {
            if (carIngredient.getCurrentQuantity() > 0) {
                Inventory inventoryChange = inventoryService.modifyInventory(new Inventory(carIngredient.getCarByCarId(), 0,
                        carIngredient.getCurrentQuantity(), carIngredient.getIngredientByIngredientId()), true);
                carIngredient.setCurrentQuantity(0);
                ingredients.put(carIngredient, inventoryChange);
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

    @Override
    @Transactional
    public void persist(Object entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

}
