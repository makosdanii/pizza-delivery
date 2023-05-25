package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends CrudRepository<Car, Integer> {
    List<Car> findByLicense(String license);

    List<Car> findAll();

    List<Car> findAllByUserByUserIdOrUserByUserIdIsNull(User userByUserId);
}
