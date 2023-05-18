package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.CarIngredient;
import com.pizzadelivery.server.data.entities.CarIngredientPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CarIngredientRepository extends CrudRepository<CarIngredient, CarIngredientPK> {
    public List<CarIngredient> findAllByIdCarByCarId(Car car);

    public void deleteAllByIdCarByCarId(Car car);
}
