package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Embeddable
public class OrderDeliveryPK implements Serializable {
    @Basic
    @Column(name = "delivered_at", nullable = false)
    private Timestamp deliveredAt;
    @NotNull
    @OneToOne
    @JoinColumn(name = "food_order_id", referencedColumnName = "id", nullable = false)
    private FoodOrder foodOrderByFoodOrderId;

    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public FoodOrder getFoodOrderByFoodOrderId() {
        return foodOrderByFoodOrderId;
    }

    public void setFoodOrderByFoodOrderId(FoodOrder foodOrderByFoodOrderId) {
        this.foodOrderByFoodOrderId = foodOrderByFoodOrderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDeliveryPK that = (OrderDeliveryPK) o;
        return Objects.equals(deliveredAt, that.deliveredAt) && Objects.equals(foodOrderByFoodOrderId, that.foodOrderByFoodOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveredAt, foodOrderByFoodOrderId);
    }

    public OrderDeliveryPK() {
        deliveredAt = new Timestamp(System.currentTimeMillis());
    }

    public OrderDeliveryPK(FoodOrder foodOrderByFoodOrderId) {
        deliveredAt = new Timestamp(System.currentTimeMillis());
        this.foodOrderByFoodOrderId = foodOrderByFoodOrderId;
    }
}
