package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Embeddable
public class OrderDeliveryPK implements Serializable {
    @CreationTimestamp
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    @Column(name = "delivered_at", nullable = false)
    private Date deliveredAt;
    @NotNull
    @OneToOne
    @JoinColumn(name = "food_order_id", referencedColumnName = "id", nullable = false)
    private FoodOrder foodOrderByFoodOrderId;

    public Date getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Date deliveredAt) {
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
    }

    public OrderDeliveryPK(FoodOrder foodOrderByFoodOrderId) {
        this.foodOrderByFoodOrderId = foodOrderByFoodOrderId;
    }
}
