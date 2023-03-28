package com.pizzadelivery.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class AuthenticationDTO implements Serializable {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public AuthenticationDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthenticationDTO() {
    }
}
