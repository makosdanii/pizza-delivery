package com.pizzadelivery.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Data transit object for {@code AuthenticationController}
 */
public class AuthenticationDTO implements Serializable {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private boolean rememberMe;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public AuthenticationDTO() {
    }

    public AuthenticationDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
