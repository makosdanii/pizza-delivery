package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Ingredient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends CrudRepository<Ingredient, Integer> {
    public List<Ingredient> findByName(String name);
}
