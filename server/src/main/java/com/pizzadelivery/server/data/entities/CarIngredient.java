package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "car_ingredient", schema = "pizza_delivery", catalog = "")
public class CarIngredient {
    @EmbeddedId
    private CarIngredientPK id;
    @Basic
    @Column(name = "current_percent", nullable = false)
    private byte currentPercent;
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    public byte getCurrentPercent() {
        return currentPercent;
    }

    public void setCurrentPercent(byte currentPercent) {
        this.currentPercent = currentPercent;
    }

    public Timestamp getModifiedAt() {
        return id.getModifiedAt();
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        id.setModifiedAt(modifiedAt);
    }

    public Car getCarByCarId() {
        return id.getCarByCarId();
    }

    public void setCarByCarId(Car carByCarId) {
        id.setCarByCarId(carByCarId);
    }

    public Ingredient getIngredientByIngredientId() {
        return ingredientByIngredientId;
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    public CarIngredientPK getId() {
        return id;
    }

    public void setId(CarIngredientPK id) {
        this.id = id;
    }
}
