package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;

import java.util.Collection;
import java.util.Objects;

@Entity
public class Car {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "license", nullable = true, length = 16, unique = true)
    private String license;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userByUserId;
    @OneToMany(mappedBy = "id.carByCarId")
    private Collection<CarIngredient> carIngredientsById;
    @OneToMany(mappedBy = "carByCarId")
    private Collection<FoodOrder> foodOrdersById;

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

    public Collection<FoodOrder> getFoodOrdersById() {
        return foodOrdersById;
    }

    public void setFoodOrdersById(Collection<FoodOrder> foodOrdersById) {
        this.foodOrdersById = foodOrdersById;
    }
}
