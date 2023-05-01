package com.pizzadelivery.server.data.entities;

import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Null;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Embeddable
public class InventoryPK implements Serializable {
    @Basic
    @Column(name = "modified_at", nullable = false)
    private Timestamp modifiedAt;
    @Null(groups = NonValidatedOnPersistTime.class)
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
        InventoryPK inventory = (InventoryPK) o;
        return Objects.equals(modifiedAt, inventory.modifiedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifiedAt);
    }

    public InventoryPK() {
        modifiedAt = new Timestamp(System.currentTimeMillis());
    }

    public InventoryPK(Car carByCarId) {
        modifiedAt = new Timestamp(System.currentTimeMillis());
        this.carByCarId = carByCarId;
    }
}
