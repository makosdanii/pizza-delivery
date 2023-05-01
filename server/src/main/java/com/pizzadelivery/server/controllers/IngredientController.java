package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.IngredientService;
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
@RequestMapping("/ingredient")
@PreAuthorize("hasAnyAuthority('admin', 'chef')")
public class IngredientController extends Controller {
    private IngredientService ingredientService;

    @Autowired
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> findIngredient(@PathVariable @Positive int id) {
        Ingredient ingredient = ingredientService.findIngredient(id);

        return new ResponseEntity<>(ingredient, ingredient.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @GetMapping
    public Iterable<Ingredient> listIngredient() {
        return ingredientService.listAll();
    }

    @PostMapping("/add")
    public ResponseEntity<Ingredient> registerIngredient(@RequestBody @Valid Ingredient ingredient) {
        try {
            ingredient = ingredientService.createIngredient(ingredient);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Ingredient());
        }
        return new ResponseEntity<>(ingredient, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> updateIngredient(@PathVariable @Positive int id, @RequestBody @Valid Ingredient ingredient) {
        try {
            ingredient = ingredientService.updateIngredient(id, ingredient);
        } catch (AlreadyExistsException e) {
            return ResponseEntity.ok(new Ingredient());
        }

        return new ResponseEntity<>(ingredient, ingredient.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteIngredient(@PathVariable @Positive int id) {
        return new ResponseEntity<>("", ingredientService.deleteIngredient(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
