package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.CarIngredient;
import com.pizzadelivery.server.data.entities.CarIngredientPK;
import com.pizzadelivery.server.data.entities.Ingredient;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CarIngredientRepository extends CrudRepository<CarIngredient, CarIngredientPK> {
    public List<CarIngredient> findAllByIdCarByCarIdAndIngredientByIngredientId(Car car, Ingredient ingredient, Sort sort);
}
