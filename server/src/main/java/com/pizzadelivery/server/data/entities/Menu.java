package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Collection;
import java.util.Objects;

@Entity
public class Menu {
    //@Null(groups = NonValidatedOnPersistTime.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @NotBlank
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    @Positive
    @Basic
    @Column(name = "price", nullable = false)
    private int price;
    @JsonManagedReference
    @UniqueElements
    @OneToMany(mappedBy = "id.menuByMenuId")
    private Collection<MenuIngredient> menuIngredientsById;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return id == menu.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Collection<MenuIngredient> getMenuIngredientsById() {
        return menuIngredientsById;
    }

    public void setMenuIngredientsById(Collection<MenuIngredient> menuIngredientsById) {
        this.menuIngredientsById = menuIngredientsById;
    }

    public Menu() {
    }

    // for testing
    public Menu(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public Menu(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
