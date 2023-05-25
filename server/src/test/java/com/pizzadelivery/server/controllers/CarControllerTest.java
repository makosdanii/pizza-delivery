package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.User;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CarControllerTest extends ControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Order(1)
    @Test
    void registerCar() throws Exception {
        var empty = new ObjectMapper().writeValueAsString(new Car());
        final var valid = new ObjectMapper().writeValueAsString(new Car("asd-123"));
        var validCar = new ObjectMapper().writeValueAsString(new Car("xxx-123"));

        //admin needs to authenticate
        Assertions.assertThrows(ServletException.class, () ->
                mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(empty)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(validCar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Order(2)
    @Test
    void updateCar() throws Exception {
        var qwe = new ObjectMapper().writeValueAsString(new Car("qwe-123",
                new User(5, "driver@domain.com")));

        authorize("driver@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/99")
                        .content(qwe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/1")
                        .content(qwe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Order(3)
    @Test
    void deleteCar() throws Exception {
        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/car/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders
                        .delete("/car/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}