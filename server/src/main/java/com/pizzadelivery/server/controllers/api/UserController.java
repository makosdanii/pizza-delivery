package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.controllers.Controller;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Validated
@RestController
@CrossOrigin
@RequestMapping(path = "/user")
public class UserController extends Controller {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('admin') || principal.getId() == #id")
    @GetMapping("/{id}")
    public ResponseEntity<User> findUser(@PathVariable @Positive @P("id") int id) {
        User user = userService.findUser(id);
        return new ResponseEntity<>(user, user.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping
    public Iterable<User> listUser() {
        return userService.listAll();
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody @Validated(NonValidatedOnPersistTime.class) User user) throws AlreadyExistsException {
        return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin') || principal.getId() == #id")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @Positive @P("id") int id, @RequestBody @Valid User user) throws AlreadyExistsException {
        user = userService.updateUser(id, user);
        return new ResponseEntity<>(user, user.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin') || principal.getId() == #id")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable @Positive @P("id") int id) {
        return new ResponseEntity<>("", userService.deleteUser(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("principal.getId() == #id")
    @PostMapping("/{id}/order")
    public ResponseEntity<Integer> placeOrder(@PathVariable @Positive @P("id") int id,
                                              @RequestBody @Validated(NonValidatedOnPersistTime.class) List<FoodOrder> foodOrders) {
        var order = userService.placeOrder(id, foodOrders);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
