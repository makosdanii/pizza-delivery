package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.config.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.CarService;
import com.pizzadelivery.server.services.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Validated
@RestController
@RequestMapping("/car")
public class CarController extends Controller {
    private CarService carService;
    private RoleService roleService;

    @Autowired
    public CarController(CarService carService, RoleService roleService) {
        this.carService = carService;
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/{id}")
    public ResponseEntity<Car> findCar(@PathVariable @Positive int id) {
        Car car = carService.findCar(id).orElse(new Car());

        return new ResponseEntity<>(car, car.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping("/add")
    public ResponseEntity<Car> registerCar(@RequestBody @Valid Car car) {
        try {
            car = carService.createCar(car);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Car());
        }
        return new ResponseEntity<>(car, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable @Positive int id, @RequestBody @Valid Car car) {
        try {
            car = carService.updateCar(id, car);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Car());
        }

        return new ResponseEntity<>(car, car.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCar(@PathVariable @Positive int id) {
        return new ResponseEntity<>("", carService.deleteCar(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'driver')")
    @PostMapping("/inventory")
    public ResponseEntity<Inventory> modifyInventory(@RequestBody @Valid Inventory inventory) {
        return new ResponseEntity<>(carService.modifyInventory(inventory), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'driver')")
    @PostMapping("/{id}/fill")
    public ResponseEntity<Car> fillIngredient(@PathVariable @Positive @P("id") int id, @RequestBody @Valid CarIngredient carIngredient) {
        if (isDriverAuthorizedForCar(id)
                || SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("admin"))
        ) {
            Car car = carService.fillIngredient(id, carIngredient);
            return ResponseEntity.ok(car);
        }

        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyAuthority('admin', 'driver')")
    @PostMapping("/{id}/deplete")
    public ResponseEntity<Car> depleteIngredient(@PathVariable @Positive @P("id") int id, @RequestBody @Valid CarIngredient carIngredient) {
        if (isDriverAuthorizedForCar(id)
                || SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("admin"))
        ) {
            Car car = carService.depleteIngredient(id, carIngredient);
            return ResponseEntity.ok(car);
        }

        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyAuthority('driver')")
    @PostMapping("/{id}/delivery")
    public ResponseEntity<Car> deliverOrder(@PathVariable @Positive @P("id") int id, @RequestBody @Validated(NonValidatedOnPersistTime.class) OrderDelivery orderDelivery) {
        if (isDriverAuthorizedForCar(id)) {
            Car car = carService.deliverOrder(id, orderDelivery);
            return ResponseEntity.ok(car);
        }

        return ResponseEntity.notFound().build();
    }

    private boolean isDriverAuthorizedForCar(int id) {
        var car = carService.findCar(id).orElse(new Car());
        int owner = Optional.ofNullable(car.getUserByUserId()).orElse(new User()).getId();

        int driver = ((UserAuthorizationDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();

        return owner == driver;
    }
}
