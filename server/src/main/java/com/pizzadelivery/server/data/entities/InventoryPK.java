package com.pizzadelivery.server.data.entities;

import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class InventoryPK implements Serializable {
    @CreationTimestamp
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    @Column(name = "modified_at", nullable = false)
    private Date modifiedAt;
    @Null(groups = NonValidatedOnPersistTime.class)
    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id", nullable = false)
    private Car carByCarId;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
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
    }

    public InventoryPK(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    public InventoryPK(Car carByCarId, Ingredient ingredientByIngredientId) {
        this.carByCarId = carByCarId;
        this.ingredientByIngredientId = ingredientByIngredientId;
    }
}
