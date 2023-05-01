package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CarControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@domain.com", "secret",
                        List.of(new SimpleGrantedAuthority("admin"))));
    }

    @Order(1)
    @Test
    void registerCar() throws Exception {
        var bad = new ObjectMapper().writeValueAsString(new Car());
        var valid = new ObjectMapper().writeValueAsString(new Car("asd-123"));
        var duplicate = new ObjectMapper().writeValueAsString(new Car("asd-123"));
        var validWithDriver = new ObjectMapper().writeValueAsString(new Car("qwe-123",
                new User(11, "driver@domain.com")));
        var invalidNotDriver = new ObjectMapper().writeValueAsString(new Car("xxx-123",
                new User(10, "notdriver@domain.com")));

        mvc.perform(MockMvcRequestBuilders
                        .post("/car/add")
                        .content(bad)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car/add")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car/add")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car/add")
                        .content(validWithDriver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car/add")
                        .content(invalidNotDriver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void findCar() {
    }

    @Test
    void updateCar() throws Exception {
        var valid = new ObjectMapper().writeValueAsString(new Car(1, "asd-456"));

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/1111")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //TODO show that license duplicate cannot happen and original would return

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("pilot@domain.com", "verysecret",
                        List.of(new SimpleGrantedAuthority("driver"))));

        // any driver can hop into an empty car
        var validEmptyCar = new ObjectMapper().writeValueAsString(new Car(1, "asd-123",
                new User(12, "pilot@domain.com")));

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/1")
                        .content(validEmptyCar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        // needs the token which belongs to the driver who sits in the car
        var hijackingOccupiedCar = new ObjectMapper().writeValueAsString(new Car(2, "qwe-123",
                new User(12, "pilot@domain.com")));

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/2")
                        .content(hijackingOccupiedCar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());


        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("driver@domain.com", "verysecret",
                        List.of(new SimpleGrantedAuthority("driver"))));

        var emptyingCar = new ObjectMapper().writeValueAsString(new Car(2, "qwe-123", null));

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/2")
                        .content(validEmptyCar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

    }

    @Test
    void deleteCar() {
    }
}