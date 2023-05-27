package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "food_order", schema = "pizza_delivery")
public class FoodOrder {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Basic
    @Column(name = "ordered_at", nullable = false)
    private Date orderedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userByUserId;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "id", nullable = false)
    private Menu menuByMenuId;

    @Null(groups = NonValidatedOnPersistTime.class)
    @JsonIgnore
    @OneToOne(mappedBy = "id.foodOrderByFoodOrderId", orphanRemoval = true)
    private OrderDelivery orderDeliveryByOrderDeliveryId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Date orderedAt) {
        this.orderedAt = orderedAt;
    }

    public User getUserByUserId() {
        return userByUserId;
    }

    public void setUserByUserId(User userByUserId) {
        this.userByUserId = userByUserId;
    }

    public Menu getMenuByMenuId() {
        return menuByMenuId;
    }

    public void setMenuByMenuId(Menu menuByMenuId) {
        this.menuByMenuId = menuByMenuId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodOrder foodOrder = (FoodOrder) o;
        return id == foodOrder.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public FoodOrder() {
    }

    // for testing
    public FoodOrder(User userByUserId, Menu menuByMenuId) {
        this.userByUserId = userByUserId;
        this.menuByMenuId = menuByMenuId;
    }
}
