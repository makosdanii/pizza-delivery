package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.data.repositories.AllergyRepository;
import com.pizzadelivery.server.data.repositories.IngredientRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class IngredientService implements ServiceORM<Ingredient> {
    IngredientRepository ingredientRepository;
    AllergyRepository allergyRepository;

    @Autowired
    public IngredientService(IngredientRepository ingredientRepository, AllergyRepository allergyRepository) {
        this.ingredientRepository = ingredientRepository;
        this.allergyRepository = allergyRepository;
    }

    public Ingredient createIngredient(Ingredient ingredient) throws AlreadyExistsException {
        checkConstraint(ingredient, true);
        ingredient.setId(UNASSIGNED);
        return ingredientRepository.save(ingredient);
    }

    public Ingredient findIngredient(int id) {
        return ingredientRepository.findById(id).orElse(new Ingredient());
    }

    public Iterable<Ingredient> listAll() {
        return ingredientRepository.findAll();
    }

    public Ingredient updateIngredient(int id, Ingredient ingredient) throws AlreadyExistsException {
        Ingredient old = ingredientRepository.findById(id).orElse(new Ingredient());
        if (old.getId() != UNASSIGNED) {
            checkConstraint(ingredient, !old.getName().equals(ingredient.getName()));
            ingredient.setId(id);
            return ingredientRepository.save(ingredient);
        }
        return old;
    }

    public boolean deleteIngredient(int id) {
        if (!ingredientRepository.existsById(id)) {
            return false;
        }

        ingredientRepository.deleteById(id);
        return true;
    }

    @Override
    public void checkConstraint(Ingredient ingredient, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !ingredientRepository.findByName(ingredient.getName()).isEmpty()) {
            throw new AlreadyExistsException();
        }

        if (ingredient.getAllergyByAllergyId() != null && !allergyRepository.existsById(ingredient.getAllergyByAllergyId().getId())) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }
}
