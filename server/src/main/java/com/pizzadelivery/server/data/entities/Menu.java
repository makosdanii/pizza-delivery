package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.Objects;

@Entity
public class Menu {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    @Basic
    @Column(name = "price", nullable = false)
    private byte price;
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

    public byte getPrice() {
        return price;
    }

    public void setPrice(byte price) {
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
}
