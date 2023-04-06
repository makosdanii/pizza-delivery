package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

@Entity
public class Inventory {
    @EmbeddedId
    private InventoryPK id;
    @PositiveOrZero
    @Basic
    @Column(name = "expense", nullable = true)
    private Integer expense;
    @Positive
    @Basic
    @Column(name = "current_qt", nullable = false)
    private int currentQt;

    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    public InventoryPK getId() {
        return id;
    }

    public void setId(InventoryPK id) {
        this.id = id;
    }

    public Integer getExpense() {
        return expense;
    }

    public void setExpense(Integer expense) {
        this.expense = expense;
    }

    public int getCurrentQt() {
        return currentQt;
    }

    public void setCurrentQt(int currentQt) {
        this.currentQt = currentQt;
    }

    public Timestamp getModifiedAt() {
        return id.getModifiedAt();
    }

    public Ingredient getIngredientByIngredientId() {
        return ingredientByIngredientId;
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
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

    public Inventory() {
        id = new InventoryPK();
    }
}
