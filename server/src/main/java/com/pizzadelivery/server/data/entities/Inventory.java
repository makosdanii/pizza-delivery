package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Inventory {
    @EmbeddedId
    private InventoryPK id;
    @Basic
    @Column(name = "expense", nullable = true)
    private Byte expense;
    @Basic
    @Column(name = "current_qt", nullable = false)
    private int currentQt;
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    public Byte getExpense() {
        return expense;
    }

    public void setExpense(Byte expense) {
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

    public InventoryPK getId() {
        return id;
    }

    public void setId(InventoryPK id) {
        this.id = id;
    }
}
