package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Collection;
import java.util.Objects;

@Entity
public class Car {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @NotBlank
    @Column(name = "license", length = 16, unique = true)
    private String license;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userByUserId;
    @JsonIgnore
    @UniqueElements
    @OneToMany(mappedBy = "id.carByCarId")
    private Collection<CarIngredient> carIngredientsById;
    @JsonIgnore
    @UniqueElements
    @OneToMany(mappedBy = "carByCarId")
    private Collection<OrderDelivery> orderDeliveriesById;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return id == car.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public User getUserByUserId() {
        return userByUserId;
    }

    public void setUserByUserId(User userByUserId) {
        this.userByUserId = userByUserId;
    }

    public Collection<CarIngredient> getCarIngredientsById() {
        return carIngredientsById;
    }

    public void setCarIngredientsById(Collection<CarIngredient> carIngredientsById) {
        this.carIngredientsById = carIngredientsById;
    }

    public Collection<OrderDelivery> getOrderDeliveriesById() {
        return orderDeliveriesById;
    }

    public void setOrderDeliveriesById(Collection<OrderDelivery> foodOrdersById) {
        this.orderDeliveriesById = foodOrdersById;
    }

    public Car() {
    }

    // for testing
    public Car(String license) {
        this.license = license;
    }

    public Car(int id, String license) {
        this.id = id;
        this.license = license;
    }

    public Car(String license, User userByUserId) {
        this.license = license;
        this.userByUserId = userByUserId;
    }

    public Car(int id, String license, User userByUserId) {
        this.id = id;
        this.license = license;
        this.userByUserId = userByUserId;
    }
}
