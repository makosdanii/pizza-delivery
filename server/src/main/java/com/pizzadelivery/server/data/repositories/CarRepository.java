package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Car;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends CrudRepository<Car, Integer> {

}
