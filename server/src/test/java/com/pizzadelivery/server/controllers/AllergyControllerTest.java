package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.Allergy;
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
class AllergyControllerTest extends ControllerTest {
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
    void registerAllergy() throws Exception {
        var empty = new ObjectMapper().writeValueAsString(new Allergy());
        final var valid = new ObjectMapper().writeValueAsString(new Allergy("gluten"));
        var properLactose = new ObjectMapper().writeValueAsString(new Allergy("lactose"));
        var duplicate = new ObjectMapper().writeValueAsString(new Allergy("gluten"));

        authorize("john.smith@example.com");

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

        var result = mvc.perform(MockMvcRequestBuilders
                        .post("/allergy")
                        .content(properLactose)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andReturn();

        created = new ObjectMapper()
                .createParser(result
                        .getResponse().getContentAsString())
                .readValueAs(Allergy.class)
                .getId();

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

        authorize("john.smith@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .put("/allergy/99")
                        .content(glucose)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(MockMvcRequestBuilders
                        .put("/allergy/" + created)
                        .content(glucose)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        var alreadyExist = new ObjectMapper().writeValueAsString(new Allergy("gluten"));
        mvc.perform(MockMvcRequestBuilders
                        .put("/allergy/" + created)
                        .content(alreadyExist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @Test
    void deleteAllergy() throws Exception {
        authorize("john.smith@example.com");

        mvc.perform(MockMvcRequestBuilders
                        .delete("/allergy/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(MockMvcRequestBuilders
                        .delete("/allergy/" + created)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}