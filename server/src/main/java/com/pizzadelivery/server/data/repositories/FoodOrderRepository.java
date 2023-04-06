package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.FoodOrder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodOrderRepository extends CrudRepository<FoodOrder, Integer> {
}
