package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Menu;
import com.pizzadelivery.server.data.entities.MenuIngredient;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.MenuService;
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
@RequestMapping("/menu")
public class MenuController extends Controller {
    private MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PreAuthorize("hasAnyAuthority('admin', 'chef', 'customer', 'driver')")
    @GetMapping("/{id}")
    public ResponseEntity<Menu> findMenu(@PathVariable @Positive int id) {
        Menu menu = menuService.findMenu(id);

        return new ResponseEntity<>(menu, menu.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'chef')")
    @PostMapping("/add")
    public ResponseEntity<Menu> registerMenu(@RequestBody @Valid Menu menu) {
        try {
            menu = menuService.createMenu(menu);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Menu());
        }
        return new ResponseEntity<>(menu, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'chef')")
    @PostMapping("/{id}/ingredient")
    public ResponseEntity<Menu> assignIngredient(@PathVariable @Positive int id, @RequestBody @Valid MenuIngredient menuIngredient) {
        Menu menu = menuService.assignIngredient(id, menuIngredient);
        return new ResponseEntity<>(menu, menu.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'chef')")
    @DeleteMapping("/{id}/ingredient")
    public ResponseEntity<Menu> unAssignIngredient(@PathVariable @Positive int id, @RequestBody @Valid MenuIngredient menuIngredient) {
        Menu menu = menuService.unAssignIngredient(id, menuIngredient);
        return new ResponseEntity<>(menu, menu.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'chef')")
    @PutMapping("/{id}")
    public ResponseEntity<Menu> updateMenu(@PathVariable @Positive int id, @RequestBody @Valid Menu menu) {
        try {
            menu = menuService.updateMenu(id, menu);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Menu());
        }

        return new ResponseEntity<>(menu, menu.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyAuthority('admin', 'chef')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMenu(@PathVariable @Positive int id) {
        return new ResponseEntity<>("", menuService.deleteMenu(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
