package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Objects;

@Entity
public class Ingredient {
    @Null
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @NotBlank
    @Column(name = "name", nullable = false, length = 64)
    private String name;
    @ManyToOne
    @NotNull
    @JoinColumn(name = "allergy_id", referencedColumnName = "id")
    private Allergy allergyByAllergyId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Allergy getAllergyByAllergyId() {
        return allergyByAllergyId;
    }

    public void setAllergyByAllergyId(Allergy allergyByAllergyId) {
        this.allergyByAllergyId = allergyByAllergyId;
    }
}
