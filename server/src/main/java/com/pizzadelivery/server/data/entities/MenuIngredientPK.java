package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MenuIngredientPK implements Serializable {
    @JsonBackReference
    @Null(groups = NonValidatedOnPersistTime.class)
    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "id", nullable = false)
    private Menu menuByMenuId;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "ingredient_id", referencedColumnName = "id", nullable = false)
    private Ingredient ingredientByIngredientId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuIngredientPK that = (MenuIngredientPK) o;
        return menuByMenuId.getId() == that.menuByMenuId.getId()
                && ingredientByIngredientId.getId() == that.ingredientByIngredientId.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuByMenuId.getId(), ingredientByIngredientId.getId());
    }

    public Menu getMenuByMenuId() {
        return menuByMenuId;
    }

    public void setMenuByMenuId(Menu menuByMenuId) {
        this.menuByMenuId = menuByMenuId;
    }

    public Ingredient getIngredientByIngredientId() {
        return ingredientByIngredientId;
    }

    public void setIngredientByIngredientId(Ingredient ingredientByIngredientId) {
        this.ingredientByIngredientId = ingredientByIngredientId;
    }

    public MenuIngredientPK() {
    }

    public MenuIngredientPK(Menu menuByMenuId, Ingredient ingredientByIngredientId) {
        this.menuByMenuId = menuByMenuId;
        this.ingredientByIngredientId = ingredientByIngredientId;
    }
}
