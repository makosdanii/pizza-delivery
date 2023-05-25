package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Allergy;
import com.pizzadelivery.server.data.entities.Ingredient;
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
class IngredientControllerTest extends ControllerTest {
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
    void registerIngredient() throws Exception {
        var valid = new ObjectMapper().writeValueAsString(new Ingredient("cheese", 10));
        var duplicate = new ObjectMapper().writeValueAsString(new Ingredient("cheese", 10));
        var validWithAllergy = new ObjectMapper().writeValueAsString(new Ingredient("flour", 5,
                new Allergy(1, "gluten")));
        var inValidAllergy = new ObjectMapper().writeValueAsString(new Ingredient("flour", 5,
                new Allergy(99, "invalid")));
        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient")
                        .content(validWithAllergy)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient")
                        .content(inValidAllergy)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(2)
    @Test
    void updateIngredient() throws Exception {
        var flour = new ObjectMapper().writeValueAsString(new Ingredient("ham", 10));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/ingredient/99")
                        .content(flour)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/ingredient/2")
                        .content(flour)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }
}