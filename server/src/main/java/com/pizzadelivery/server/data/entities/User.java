package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.Objects;

@Entity
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Email
    @NotBlank
    @Column(name = "email", nullable = false, length = 64)
    private String email;
    @Basic
    @NotBlank
    @Column(name = "name", length = 64)
    private String name;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Basic
    @NotBlank
    @Column(name = "password", nullable = false, length = 128)
    private String password;
    @JsonIgnore
    @OneToMany(mappedBy = "userByUserId")
    private Collection<Car> carsById;
    @JsonIgnore
    @OneToMany(mappedBy = "userByUserId", orphanRemoval = true)
    private Collection<FoodOrder> foodOrdersById;
    @ManyToOne
    @JoinColumn(name = "street_name_id", referencedColumnName = "id")
    private StreetName streetNameByStreetNameId;
    @Basic
    @Column(name = "house_no")
    private int houseNo;
    @ManyToOne
    @NotNull
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role roleByRoleId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Collection<Car> getCarsById() {
        return carsById;
    }

    public void setCarsById(Collection<Car> carsById) {
        this.carsById = carsById;
    }

    public Collection<FoodOrder> getFoodOrdersById() {
        return foodOrdersById;
    }

    public void setFoodOrdersById(Collection<FoodOrder> foodOrdersById) {
        this.foodOrdersById = foodOrdersById;
    }

    public StreetName getStreetNameByStreetNameId() {
        return streetNameByStreetNameId;
    }

    public void setStreetNameByStreetNameId(StreetName streetNameByStreetNameId) {
        this.streetNameByStreetNameId = streetNameByStreetNameId;
    }

    public int getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(int houseNo) {
        this.houseNo = houseNo;
    }

    public Role getRoleByRoleId() {
        return roleByRoleId;
    }

    public void setRoleByRoleId(Role roleByRoleId) {
        this.roleByRoleId = roleByRoleId;
    }

    public User() {
    }

    // for testing
    public User(int id, String email) {
        this.id = id;
        this.email = email;
    }

    public User(String email, String name, String password, Role roleByRoleId) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.roleByRoleId = roleByRoleId;
    }

    public User(String email, String name, String password, StreetName streetNameByStreetNameId, int houseNo, Role roleByRoleId) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.streetNameByStreetNameId = streetNameByStreetNameId;
        this.houseNo = houseNo;
        this.roleByRoleId = roleByRoleId;
    }

    public User(int id, String email, StreetName streetNameByStreetNameId, int houseNo) {
        this.id = id;
        this.email = email;
        this.streetNameByStreetNameId = streetNameByStreetNameId;
        this.houseNo = houseNo;
    }
}
