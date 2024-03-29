package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "menu_ingredient", schema = "pizza_delivery")
public class MenuIngredient {
    @JsonIgnore
    @EmbeddedId
    private MenuIngredientPK id;
    @Positive
    @Basic
    @Column(name = "quantity", nullable = false)
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @JsonBackReference
    public Menu getMenuByMenuId() {
        return id.getMenuByMenuId();
    }

    public void setMenuByMenuId(Menu menuByMenuId) {
        id.setMenuByMenuId(menuByMenuId);
    }

    public Ingredient getIngredientByIngredientId() {
        return id.getIngredientByIngredientId();
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        id.setIngredientByIngredientId(ingredientByIngredientId);
    }

    public MenuIngredientPK getId() {
        return id;
    }

    public void setId(MenuIngredientPK id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuIngredient that = (MenuIngredient) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public MenuIngredient() {
        id = new MenuIngredientPK();
    }

    // for testing
    public MenuIngredient(Ingredient ingredient, int quantity) {
        this.id = new MenuIngredientPK();
        setIngredientByIngredientId(ingredient);
        this.quantity = quantity;
    }
}
