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
class MenuControllerTest {
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
    void registerMenu() throws Exception {
        var bad = new ObjectMapper().writeValueAsString(new Menu());
        var valid = new ObjectMapper().writeValueAsString(new Menu("pizza", 19));
        var duplicate = new ObjectMapper().writeValueAsString(new Menu("pizza", 199));

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/add")
                        .content(bad)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/add")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/add")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void findMenu() {
    }

    @Test
    void assignIngredient() throws Exception {
        var nonExistent = new ObjectMapper().writeValueAsString(
                new MenuIngredient(new Ingredient(32, "footLetuce"), 456));
        var valid = new ObjectMapper().writeValueAsString(
                new MenuIngredient(new Ingredient(1, "flour"), 20));
        var validUpdate = new ObjectMapper().writeValueAsString(
                new MenuIngredient(new Ingredient(1, "flour"), 25));

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/1")
                        .content(nonExistent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/111")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/1")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders
                        .post("/menu/1")
                        .content(validUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void unAssignIngredient() {
    }

    @Test
    void updateMenu() {
    }

    @Test
    void deleteMenu() {
    }
}