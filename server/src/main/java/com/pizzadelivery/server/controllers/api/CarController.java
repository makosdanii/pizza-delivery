package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.controllers.Controller;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.CarService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Validated
@RestController
@CrossOrigin
@RequestMapping("/car")
public class CarController extends Controller {
    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping("/{id}")
    public ResponseEntity<Car> findCar(@PathVariable @Positive int id) {
        Car car = carService.findCar(id);
        return new ResponseEntity<>(car, car.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'driver')")
    @GetMapping
    public Iterable<Car> listCar() {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("admin"));
        return carService.listAll(isAdmin);
    }

    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    public ResponseEntity<Car> registerCar(@RequestBody @Valid Car car) throws AlreadyExistsException {
        return new ResponseEntity<>(carService.createCar(car), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'driver')")
    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable @Positive int id, @RequestBody @Valid Car car) throws AlreadyExistsException {
        car = carService.updateCar(id, car);
        if (car.getId() != UNASSIGNED) {
            carService.persist(car);
        }

        return new ResponseEntity<>(car, car.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCar(@PathVariable @Positive int id) {
        return new ResponseEntity<>("", carService.deleteCar(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
