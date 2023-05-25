package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Allergy;
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
class AllergyControllerTest extends ControllerTest {
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
    void registerAllergy() throws Exception {
        var empty = new ObjectMapper().writeValueAsString(new Allergy());
        final var valid = new ObjectMapper().writeValueAsString(new Allergy("gluten"));
        var properLactose = new ObjectMapper().writeValueAsString(new Allergy("lactose"));
        var duplicate = new ObjectMapper().writeValueAsString(new Allergy("gluten"));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/allergy")
                        .content(empty)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/allergy")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/allergy")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/allergy")
                        .content(properLactose)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/allergy")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(2)
    @Test
    void updateAllergy() throws Exception {
        var glucose = new ObjectMapper().writeValueAsString(new Allergy("glucose"));

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/allergy/99")
                        .content(glucose)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/allergy/2")
                        .content(glucose)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        var alreadyExist = new ObjectMapper().writeValueAsString(new Allergy("gluten"));
        mvc.perform(MockMvcRequestBuilders
                        .put("/allergy/2")
                        .content(alreadyExist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @Test
    void deleteAllergy() throws Exception {
        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/allergy/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders
                        .delete("/allergy/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}