package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Collection;
import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Car.class
)
@Entity
public class Car {
    @Null
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @NotBlank
    @Column(name = "license", nullable = true, length = 16, unique = true)
    private String license;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userByUserId;
    @UniqueElements
    @OneToMany(mappedBy = "id.carByCarId", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private Collection<CarIngredient> carIngredientsById;
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
}
