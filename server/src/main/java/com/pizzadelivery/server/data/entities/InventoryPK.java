package com.pizzadelivery.server.data.entities;

import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Embeddable
public class InventoryPK implements Serializable {
    @Basic
    @Column(name = "modified_at", nullable = false)
    private Timestamp modifiedAt;
    @Null(groups = NonValidatedOnPersistTime.class)
    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id", nullable = false)
    private Car carByCarId;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Car getCarByCarId() {
        return carByCarId;
    }

    public void setCarByCarId(Car carByCarId) {
        this.carByCarId = carByCarId;
    }

    public Ingredient getIngredientByIngredientId() {
        return ingredientByIngredientId;
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryPK inventory = (InventoryPK) o;
        return Objects.equals(modifiedAt, inventory.modifiedAt)
                && carByCarId.equals(inventory.carByCarId)
                && ingredientByIngredientId.equals(inventory.ingredientByIngredientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifiedAt, carByCarId, ingredientByIngredientId);
    }

    public InventoryPK() {
        modifiedAt = new Timestamp(System.currentTimeMillis());
    }

    public InventoryPK(Ingredient ingredientByIngredientId) {
        modifiedAt = new Timestamp(System.currentTimeMillis());
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    public InventoryPK(Car carByCarId, Ingredient ingredientByIngredientId) {
        modifiedAt = new Timestamp(System.currentTimeMillis());
        this.carByCarId = carByCarId;
        this.ingredientByIngredientId = ingredientByIngredientId;
    }
}
