package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.entities.InventoryPK;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends CrudRepository<Inventory, InventoryPK> {
    public List<Inventory> findByIngredientByIngredientId(Ingredient ingredient, Sort sort);
}
