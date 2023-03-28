package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_ingredient", schema = "pizza_delivery", catalog = "")
public class MenuIngredient {
    @EmbeddedId
    private MenuIngredientPK id;
    @Basic
    @Column(name = "quantity", nullable = false)
    private byte quantity;

    public byte getQuantity() {
        return quantity;
    }

    public void setQuantity(byte quantity) {
        this.quantity = quantity;
    }


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
}
