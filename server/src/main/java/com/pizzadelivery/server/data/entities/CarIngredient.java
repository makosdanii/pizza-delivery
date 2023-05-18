package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "car_ingredient", schema = "pizza_delivery", catalog = "")
public class CarIngredient {
    @EmbeddedId
    private CarIngredientPK id;
    //    @Positive
    @Basic
    @Column(name = "current_quantity", nullable = false)
    private int currentQuantity;

    public CarIngredientPK getId() {
        return id;
    }

    public void setId(CarIngredientPK id) {
        this.id = id;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentPercent) {
        this.currentQuantity = currentPercent;
    }

    public Ingredient getIngredientByIngredientId() {
        return id.getIngredientByIngredientId();
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        id.setIngredientByIngredientId(ingredientByIngredientId);
    }

    public Car getCarByCarId() {
        return id.getCarByCarId();
    }

    public void setCarByCarId(Car carByCarId) {
        id.setCarByCarId(carByCarId);
    }

    public CarIngredient() {
        this.id = new CarIngredientPK();
    }

    public CarIngredient(int currentQuantity, Ingredient ingredientByIngredientId, Car carByCarId) {
        this.id = new CarIngredientPK(ingredientByIngredientId);
        id.setCarByCarId(carByCarId);
        this.currentQuantity = currentQuantity;
    }
}
