package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
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
@RequestMapping("/role")
@PreAuthorize("hasAuthority('admin')")
public class RoleController extends Controller {
    private RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> findRole(@PathVariable @Positive int id) {
        Role role = roleService.findRole(id);

        return new ResponseEntity<>(role, role.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Role> registerRole(@RequestBody @Valid Role role) {
        try {
            role = roleService.createRole(role);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Role());
        }
        return new ResponseEntity<>(role, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable @Positive int id, @RequestBody @Valid Role role) {
        try {
            role = roleService.updateRole(id, role);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Role());
        }

        return new ResponseEntity<>(role, role.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable @Positive int id) {
        return new ResponseEntity<>("", roleService.deleteRole(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
