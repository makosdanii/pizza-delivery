package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Embeddable
public class CarIngredientPK implements Serializable {
    @Basic
    @Column(name = "modified_at", nullable = false)
    private Timestamp modifiedAt;
    @Null
    @ManyToOne
    @JoinColumn(name = "car_id", referencedColumnName = "id", nullable = false)
    private Car carByCarId;

    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public Car getCarByCarId() {
        return carByCarId;
    }

    public void setCarByCarId(Car carByCarId) {
        this.carByCarId = carByCarId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarIngredientPK that = (CarIngredientPK) o;
        return Objects.equals(modifiedAt, that.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifiedAt);
    }

    public CarIngredientPK() {
        modifiedAt = new Timestamp(System.currentTimeMillis());
    }
}
