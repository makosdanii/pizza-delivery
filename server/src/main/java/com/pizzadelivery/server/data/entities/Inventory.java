package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

@Entity
public class Inventory {
    @JsonIgnore
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

    public int getQuantity() {
        return currentQt;
    }

    public void setQuantity(int currentQt) {
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

    public Inventory(Integer expense, int currentQt, Ingredient ingredientByIngredientId) {
        id = new InventoryPK();
        this.expense = expense;
        this.currentQt = currentQt;
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    public Inventory(Car carByCarId, Integer expense, int currentQt, Ingredient ingredientByIngredientId) {
        this.id = new InventoryPK(carByCarId);
        this.expense = expense;
        this.currentQt = currentQt;
        this.ingredientByIngredientId = ingredientByIngredientId;
    }
}
