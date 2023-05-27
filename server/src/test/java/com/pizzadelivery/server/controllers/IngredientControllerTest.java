package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Allergy;
import com.pizzadelivery.server.data.entities.Ingredient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
class IngredientControllerTest extends ControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;
    private static int created;

    @BeforeEach
    void setUp() {
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
        var inValidAllergy = new ObjectMapper().writeValueAsString(new Ingredient("salami", 5,
                new Allergy(99, "invalid")));
        authorize("john.smith@example.com");

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

        var result = mvc.perform(MockMvcRequestBuilders
                        .post("/ingredient")
                        .content(validWithAllergy)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        created = new ObjectMapper()
                .createParser(result
                        .getResponse().getContentAsString())
                .readValueAs(Ingredient.class)
                .getId();

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
        var ham = new ObjectMapper().writeValueAsString(new Ingredient("salami", 10));

        authorize("john.smith@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/ingredient/99")
                        .content(ham)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/ingredient/" + created)
                        .content(ham)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        var alreadyExists = new ObjectMapper().writeValueAsString(new Ingredient("cheese", 10));

        mvc.perform(MockMvcRequestBuilders
                        .put("/ingredient/" + created)
                        .content(alreadyExists)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Order(3)
    @Test
    void deleteIngredient() throws Exception {
        authorize("john.smith@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/ingredient/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders
                        .delete("/ingredient/" + created)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}