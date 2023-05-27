package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.User;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class CarControllerTest extends ControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;
    private static int created;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        SecurityContextHolder.clearContext();
    }

    @Order(1)
    @Test
    void registerCar() throws Exception {
        var empty = new ObjectMapper().writeValueAsString(new Car());
        final var valid = new ObjectMapper().writeValueAsString(new Car("asd-123"));
        var properCar = new ObjectMapper().writeValueAsString(new Car("xxx-123"));

        //admin needs to authenticate
        Assertions.assertThrows(ServletException.class, () ->
                mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)));

        authorize("john.smith@example.com");

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

        var result = mvc.perform(MockMvcRequestBuilders
                        .post("/car")
                        .content(properCar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andReturn();

        created = new ObjectMapper()
                .createParser(result
                        .getResponse().getContentAsString())
                .readValueAs(Car.class)
                .getId();

    }

    @Order(2)
    @Test
    void updateCar() throws Exception {
        var qwe = new ObjectMapper().writeValueAsString(new Car("xxx-123",
                new User(3, "david.williams@example.com")));

        authorize("david.williams@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/99")
                        .content(qwe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/" + created)
                        .content(qwe)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        authorize("john.smith@example.com");

        var alreadyExists = new ObjectMapper().writeValueAsString(new Car("asd-123",
                new User(3, "david.williams@example.com")));

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/" + created)
                        .content(alreadyExists)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        var notDriver = new ObjectMapper().writeValueAsString(new Car("qwe-123",
                new User(7, "christopher.wilson@example.com")));

        mvc.perform(MockMvcRequestBuilders
                        .put("/car/" + created)
                        .content(notDriver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @Test
    void deleteCar() throws Exception {
        authorize("john.smith@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/car/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders
                        .delete("/car/" + created)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}