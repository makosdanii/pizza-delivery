package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

import java.sql.Timestamp;

@Entity
@Table(name = "car_ingredient", schema = "pizza_delivery", catalog = "")
public class CarIngredient {
    @EmbeddedId
    private CarIngredientPK id;
    @Range(min = 0, max = 100)
    @Basic
    @Column(name = "current_percent", nullable = false)
    private int currentPercent;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    public CarIngredientPK getId() {
        return id;
    }

    public void setId(CarIngredientPK id) {
        this.id = id;
    }

    public int getCurrentPercent() {
        return currentPercent;
    }

    public void setCurrentPercent(int currentPercent) {
        this.currentPercent = currentPercent;
    }

    public Ingredient getIngredientByIngredientId() {
        return ingredientByIngredientId;
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
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

    public CarIngredient() {
        this.id = new CarIngredientPK();
    }
}
