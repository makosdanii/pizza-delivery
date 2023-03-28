package com.pizzadelivery.server.data.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class FoodOrderPK implements Serializable {
    @Basic
    @Column(name = "ordered_at", nullable = false)
    private Timestamp orderedAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userByUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodOrderPK foodOrder = (FoodOrderPK) o;
        return Objects.equals(orderedAt, foodOrder.orderedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderedAt);
    }

    public Timestamp getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Timestamp orderedAt) {
        this.orderedAt = orderedAt;
    }

    public User getUserByUserId() {
        return userByUserId;
    }

    public void setUserByUserId(User userByUserId) {
        this.userByUserId = userByUserId;
    }
}
