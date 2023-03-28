package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.RoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/role")
@PreAuthorize("hasAuthority('admin')")
public class RoleController extends Controller {
    private RoleService roleService;

    @GetMapping("/{id}")
    public ResponseEntity<Role> findUser(@PathVariable @Min(1) @P("id") int id) {
        Role role = roleService.findRole(id);

        return new ResponseEntity<>(role, role.getId() == 0 ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Role> registerUser(@RequestBody @Valid Role role) {
        try {
            role = roleService.createRole(role);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Role());
        }
        return new ResponseEntity<>(role, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateUser(@PathVariable @Min(1) int id, @RequestBody @Valid Role role) {
        role = roleService.updateRole(id, role);

        return new ResponseEntity<>(role, role.getId() == 0 ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable @Min(1) int id) {
        return new ResponseEntity<>("", roleService.deleteRole(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
