package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;

    @Autowired
    UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Order(1)
    @Test
    void registerUser() throws Exception {
        var bad = new ObjectMapper().writeValueAsString(new User());

        var better = new ObjectMapper().writeValueAsString(new User("john.doe@domain.com", "John Doe",
                "verysecret", null));

        var valid = new ObjectMapper().writeValueAsString(new User("john.doe@domain.com", "John Doe",
                "verysecret", new Role(3, "customer")));

        var validWithAddress = new ObjectMapper().writeValueAsString(new User("jane.doe@domain.com", "Jane Doe",
                "verysecret", new StreetName(1, "Baker str."), 1, new Role(3, "customer")));

        var duplicate = new ObjectMapper().writeValueAsString(new User("john.doe@domain.com", "John Doe",
                "verysecret", new Role(3, "customer")));

        var invalidMail = new ObjectMapper().writeValueAsString(new User("domain", "John Doe",
                "verysecret", new Role(3, "customer")));

        var blankName = new ObjectMapper().writeValueAsString(new User("name@domain.com", "",
                "verysecret", new Role(3, "customer")));

        var blankPassword = new ObjectMapper().writeValueAsString(new User("name@domain.com", "John Doe",
                "", new Role(3, "customer")));

        var invalidRole = new ObjectMapper().writeValueAsString(new User("name@domain.com", "John Doe",
                "verysecret", new Role(169, "customer")));

        // registration will need token
        var forbiddenRole = new ObjectMapper().writeValueAsString(new User("driver@domain.com", "John Doe",
                "verysecret", new Role(4, "driver")));

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content("'malformed': 'body'")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(bad)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(better)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(validWithAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(duplicate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(invalidMail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(blankName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(blankPassword)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(invalidRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(forbiddenRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin@domain.com", "secret",
                        List.of(new SimpleGrantedAuthority("admin"))));
        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(forbiddenRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

    }

    @Test
    void findUser() {
    }

    @Test
    void updateUser() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void placeOrder() throws Exception {
        User user = new User(9, "john.doe@domain.com", new StreetName(1, "Baker str."), 1);
        var invalid = new ObjectMapper().writeValueAsString(List.of(
                new FoodOrder(user, new Menu(32, "polyJuicePotion"))));
        var missingAddress = new ObjectMapper().writeValueAsString(List.of(
                new FoodOrder(new User(9, "john.doe@domain.com"), new Menu(1, "pizza"))));
        var valid = new ObjectMapper().writeValueAsString(List.of(
                new FoodOrder(user, new Menu(1, "pizza"))));

        UserDetails userDetails = userService.loadUserByUsername("john.doe@domain.com");

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        // user can only order for himself
//        mvc.perform(MockMvcRequestBuilders
//                        .post("/user/2/order")
//                        .content(valid)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/9/order")
                        .content(invalid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/9/order")
                        .content(missingAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());


        mvc.perform(MockMvcRequestBuilders
                        .post("/user/9/order")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}