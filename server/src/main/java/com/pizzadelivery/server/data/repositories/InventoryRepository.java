package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.entities.InventoryPK;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface InventoryRepository extends CrudRepository<Inventory, InventoryPK> {
    List<Inventory> findByIdIngredientByIngredientId(Ingredient ingredient, Sort sort);

    List<Inventory> findAll();

    List<Inventory> findAllByIdModifiedAtBefore(Date before);
}
