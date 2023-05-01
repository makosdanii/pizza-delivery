package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.CarService;
import com.pizzadelivery.server.services.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Validated
@RestController
@CrossOrigin
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

    @GetMapping
    public Iterable<Car> listCar() {
        return carService.listAll();
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

    @PreAuthorize("hasAnyAuthority('admin', 'driver')")
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
}
