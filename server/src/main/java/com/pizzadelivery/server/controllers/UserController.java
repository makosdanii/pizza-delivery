package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.RoleService;
import com.pizzadelivery.server.services.StreetService;
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

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Validated
@RestController
@RequestMapping(path = "/user")
public class UserController extends Controller {
    private UserService userService;
    private StreetService streetService;
    private RoleService roleService;

    @Autowired
    public UserController(UserService userService, StreetService streetService, RoleService roleService) {
        this.userService = userService;
        this.streetService = streetService;
        this.roleService = roleService;
    }

    @PreAuthorize("hasAuthority('admin') || principal.getId() == #id")
    @GetMapping("/{id}")
    public ResponseEntity<User> findUser(@PathVariable @Positive @P("id") int id) {
        User user = userService.findUser(id);

        return new ResponseEntity<>(user, user.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody @Valid User user) {
        try {
            user = userService.createUser(user);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new User());
        }
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin') || principal.getId() == #id")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable @Positive @P("id") int id, @RequestBody @Valid User user) {
        try {
            user = userService.updateUser(id, user);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new User());
        }

        return new ResponseEntity<>(user, user.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin') || principal.getId() == #id")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable @Positive @P("id") int id) {
        return new ResponseEntity<>("", userService.deleteUser(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PreAuthorize("principal.getId() == #id")
    @PostMapping("/{id}/order")
    public ResponseEntity<FoodOrder> placeOrder(@PathVariable @Positive @P("id") int id,
                                                @RequestBody @Validated(NonValidatedOnPersistTime.class) FoodOrder foodOrder) {
        return new ResponseEntity<>(userService.placeOrder(id, foodOrder), HttpStatus.CREATED);
    }
}
