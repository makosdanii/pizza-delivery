package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@SpringBootTest
public class ControllerTest {
    @Autowired
    UserService userService;

    protected void authorize(String email) {
        UserDetails userDetails = userService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(usernamePasswordAuthenticationToken);
    }
}
