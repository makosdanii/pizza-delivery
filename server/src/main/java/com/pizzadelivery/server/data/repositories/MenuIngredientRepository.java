package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.MenuIngredient;
import com.pizzadelivery.server.data.entities.MenuIngredientPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuIngredientRepository extends CrudRepository<MenuIngredient, MenuIngredientPK> {
}
