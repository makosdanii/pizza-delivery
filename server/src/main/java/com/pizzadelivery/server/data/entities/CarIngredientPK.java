package com.pizzadelivery.server.data.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CarIngredientPK implements Serializable {
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;
    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id", nullable = false)
    private Car carByCarId;

    public Ingredient getIngredientByIngredientId() {
        return ingredientByIngredientId;
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    public Car getCarByCarId() {
        return carByCarId;
    }

    public void setCarByCarId(Car carByCarId) {
        this.carByCarId = carByCarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarIngredientPK that = (CarIngredientPK) o;
        return this.carByCarId.equals(that.getCarByCarId())
                && this.ingredientByIngredientId.equals(that.getIngredientByIngredientId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(carByCarId, ingredientByIngredientId);
    }

    public CarIngredientPK() {
    }

    public CarIngredientPK(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
    }
}
