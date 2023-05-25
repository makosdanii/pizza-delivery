package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.data.entities.Menu;
import com.pizzadelivery.server.data.entities.MenuIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MenuControllerTest extends ControllerTest {
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
    void registerMenu() throws Exception {
        var valid = new ObjectMapper().writeValueAsString(new Menu("pizza", 100));
        var duplicate = new ObjectMapper().writeValueAsString(new Menu("pizza", 200));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(2)
    @Test
    void assignIngredient() throws Exception {
        var nonExistent = new ObjectMapper().writeValueAsString(
                new MenuIngredient(new Ingredient(99, "invalid"), 1));
        var valid = new ObjectMapper().writeValueAsString(
                new MenuIngredient(new Ingredient(1, "flour"), 20));
        var validUpdate = new ObjectMapper().writeValueAsString(
                new MenuIngredient(new Ingredient(1, "flour"), 25));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/menu/1/ingredient")
                        .content(nonExistent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/menu/1/ingredient")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .put("/menu/1/ingredient")
                        .content(validUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Order(3)
    @Test
    void unAssignIngredient() throws Exception {
        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/menu/1/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}