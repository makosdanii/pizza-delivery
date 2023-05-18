package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface FoodOrderRepository extends CrudRepository<FoodOrder, Integer> {
    public List<FoodOrder> findAll();

    public List<FoodOrder> findAllByUserByUserId(User userByUserId);

    public List<FoodOrder> findAllByOrderedAtBefore(Date before);
}
