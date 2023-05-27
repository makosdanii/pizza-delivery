package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Date;

@Entity
public class Inventory {
    @JsonIgnore
    @EmbeddedId
    private InventoryPK id;
    @PositiveOrZero
    @Basic
    @Column(name = "expense")
    private Integer expense;
    @Positive
    @Basic
    @Column(name = "current_qt", nullable = false)
    private Integer current;

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

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int currentQt) {
        this.current = currentQt;
    }

    public Date getModifiedAt() {
        return id.getModifiedAt();
    }

    public Ingredient getIngredientByIngredientId() {
        return id.getIngredientByIngredientId();
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        id.setIngredientByIngredientId(ingredientByIngredientId);
    }

    public void setModifiedAt(Date modifiedAt) {
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
        id = new InventoryPK(ingredientByIngredientId);
        this.expense = expense;
        this.current = currentQt;
    }

    public Inventory(Car carByCarId, Integer expense, int currentQt, Ingredient ingredientByIngredientId) {
        this.id = new InventoryPK(carByCarId, ingredientByIngredientId);
        this.expense = expense;
        this.current = currentQt;
    }
}
