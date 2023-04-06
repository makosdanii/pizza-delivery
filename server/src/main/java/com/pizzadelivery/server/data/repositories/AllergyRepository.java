package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Allergy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends CrudRepository<Allergy, Integer> {
    public List<Allergy> findByName(String name);
}
