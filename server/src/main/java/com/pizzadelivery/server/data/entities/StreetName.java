package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "street_name", schema = "pizza_delivery", catalog = "")
public class StreetName {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "that", nullable = false, length = 64)
    private String that;
    @Basic
    @Column(name = "until_no", nullable = false)
    private int untilNo;
    @JsonIgnore
    @OneToMany(mappedBy = "streetNameByEdgeName")
    private Collection<Edge> edgesById;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getThat() {
        return that;
    }

    public void setThat(String that) {
        this.that = that;
    }

    public int getUntilNo() {
        return untilNo;
    }

    public void setUntilNo(int untilNo) {
        this.untilNo = untilNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreetName that1 = (StreetName) o;
        return id == that1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Collection<Edge> getEdgesById() {
        return edgesById;
    }

    public void setEdgesById(Collection<Edge> edgesById) {
        this.edgesById = edgesById;
    }

    public StreetName() {
    }

    public StreetName(int id, String that) {
        this.id = id;
        this.that = that;
    }
}
