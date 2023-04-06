package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Edge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EdgeRepository extends CrudRepository<Edge, Integer> {
}
