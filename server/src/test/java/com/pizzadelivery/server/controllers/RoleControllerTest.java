package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Role;
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
class RoleControllerTest extends ControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Order(1)
    @Test
    void registerRole() throws Exception {
        var empty = new ObjectMapper().writeValueAsString(new Role());
        final var valid = new ObjectMapper().writeValueAsString(new Role("customer"));
        var properDriver = new ObjectMapper().writeValueAsString(new Role("driver"));
        var properChef = new ObjectMapper().writeValueAsString(new Role("chef"));

        //admin needs to authenticate
        Assertions.assertThrows(ServletException.class, () ->
                mvc.perform(MockMvcRequestBuilders
                        .post("/role")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/role")
                        .content(empty)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/role")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/role")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/role")
                        .content(properDriver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/role")
                        .content(properChef)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Order(2)
    @Test
    void updateRole() throws Exception {
        var cook = new ObjectMapper().writeValueAsString(new Role("cook"));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/role/99")
                        .content(cook)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/role/4")
                        .content(cook)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        var alreadyExist = new ObjectMapper().writeValueAsString(new Role("admin"));
        mvc.perform(MockMvcRequestBuilders
                        .put("/role/2")
                        .content(alreadyExist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @Test
    void deleteRole() throws Exception {
        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/role/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders
                        .delete("/role/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}