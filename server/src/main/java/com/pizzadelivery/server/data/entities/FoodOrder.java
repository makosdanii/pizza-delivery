package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "food_order", schema = "pizza_delivery", catalog = "")
public class FoodOrder {
    @EmbeddedId
    FoodOrderPK id;
    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id", nullable = false)
    private Car carByCarId;
    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "id", nullable = false)
    private Menu menuByMenuId;

    public Timestamp getOrderedAt() {
        return id.getOrderedAt();
    }

    public void setOrderedAt(Timestamp orderedAt) {
        id.setOrderedAt(orderedAt);
    }

    public User getUserByUserId() {
        return id.getUserByUserId();
    }

    public void setUserByUserId(User userByUserId) {
        id.setUserByUserId(userByUserId);
    }

    public Car getCarByCarId() {
        return carByCarId;
    }

    public void setCarByCarId(Car carByCarId) {
        this.carByCarId = carByCarId;
    }

    public Menu getMenuByMenuId() {
        return menuByMenuId;
    }

    public void setMenuByMenuId(Menu menuByMenuId) {
        this.menuByMenuId = menuByMenuId;
    }

    public FoodOrderPK getId() {
        return id;
    }

    public void setId(FoodOrderPK id) {
        this.id = id;
    }
}
