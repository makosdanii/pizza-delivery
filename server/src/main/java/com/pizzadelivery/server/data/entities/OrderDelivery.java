package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;

import java.sql.Timestamp;

@Entity
@Table(name = "order_delivery", schema = "pizza_delivery", catalog = "")
public class OrderDelivery {
    @JsonIgnore
    @Valid
    @EmbeddedId
    OrderDeliveryPK id;

    @Null(groups = NonValidatedOnPersistTime.class)
    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id", nullable = false)
    private Car carByCarId;

    public OrderDeliveryPK getId() {
        return id;
    }

    public void setId(OrderDeliveryPK id) {
        this.id = id;
    }

    public Timestamp getDeliveredAt() {
        return id.getDeliveredAt();
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        id.setDeliveredAt(deliveredAt);
    }

    public Car getCarByCarId() {
        return carByCarId;
    }

    public void setCarByCarId(Car carByCarId) {
        this.carByCarId = carByCarId;
    }

    public FoodOrder getFoodOrderByFoodOrderId() {
        return id.getFoodOrderByFoodOrderId();
    }

    public void setFoodOrderByFoodOrderId(FoodOrder foodOrderByFoodOrderId) {
        id.setFoodOrderByFoodOrderId(foodOrderByFoodOrderId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDelivery that = (OrderDelivery) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public OrderDelivery() {
        id = new OrderDeliveryPK();
    }

    public OrderDelivery(FoodOrder foodOrderByFoodOrderId, Car carByCarId) {
        this.id = new OrderDeliveryPK(foodOrderByFoodOrderId);
        this.carByCarId = carByCarId;
    }
}
