package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.StreetName;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreetRepository extends CrudRepository<StreetName, Integer> {
    public List<StreetName> findByThat(String that);
}
