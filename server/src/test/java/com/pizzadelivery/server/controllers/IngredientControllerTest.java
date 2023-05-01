package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Ingredient;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class IngredientControllerTest {
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
    void registerIngredient() throws Exception {
        var bad = new ObjectMapper().writeValueAsString(new Ingredient());
        var valid = new ObjectMapper().writeValueAsString(new Ingredient("flour"));
        var duplicate = new ObjectMapper().writeValueAsString(new Ingredient("flour"));
//        var validWithAllergy = new ObjectMapper().writeValueAsString(new Ingredient("qwe-123",
//                new User(11, "driver@domain.com")));
//        var invalidllergy = new ObjectMapper().writeValueAsString(new Ingredient("xxx-123",
//                new User(10, "notdriver@domain.com")));

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient/add")
                        .content(bad)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient/add")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient/add")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    void listIngredient() {
    }

    @Test
    void updateIngredient() {
    }

    @Test
    void deleteIngredient() {
    }
}