package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.dto.AuthenticationDTO;
import org.junit.jupiter.api.BeforeEach;
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
class AuthenticationControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void authenticateSuccessfully() throws Exception {
        var admin = new AuthenticationDTO("admin@domain.com", "secret");
        mvc.perform(MockMvcRequestBuilders
                        .post("/authenticate")
                        .content(new ObjectMapper().writeValueAsString(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
    }

    @Test
    void authenticateUnsuccessfully() throws Exception {
        var badCredentials = new AuthenticationDTO("admin@domain.com", "invalid");
        mvc.perform(MockMvcRequestBuilders
                .post("/authenticate")
                .content(new ObjectMapper().writeValueAsString(badCredentials))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());

        badCredentials = new AuthenticationDTO("invalid@domain.com", "password");
        mvc.perform(MockMvcRequestBuilders
                .post("/authenticate")
                .content(new ObjectMapper().writeValueAsString(badCredentials))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }
}