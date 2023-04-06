package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Menu;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends CrudRepository<Menu, Integer> {
    public List<Menu> findByName(String name);
}
