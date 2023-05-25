package com.pizzadelivery.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.Menu;
import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.data.entities.StreetName;
import com.pizzadelivery.server.services.UserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
class UserControllerTest extends ControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mvc;

    @Autowired
    UserService userService;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Order(1)
    @Test
    void registerUser() throws Exception {
        var valid = new ObjectMapper().writeValueAsString(new User("customer@domain.com", "John Doe",
                "secret", new Role(2, "customer")));

        var validWithAddress = new ObjectMapper().writeValueAsString(new User("jane.doe@domain.com", "Jane Doe",
                "secret", new StreetName(3, "Baker str."), 37, new Role(2, "customer")));

        var duplicate = new ObjectMapper().writeValueAsString(new User("customer@domain.com", "Duplicate Doe",
                "secret", new Role(2, "customer")));

        var invalidMail = new ObjectMapper().writeValueAsString(new User("invalid.email", "John Doe",
                "secret", new Role(2, "customer")));

        var blankName = new ObjectMapper().writeValueAsString(new User("name@domain.com", "",
                "secret", new Role(2, "customer")));

        var blankPassword = new ObjectMapper().writeValueAsString(new User("name@domain.com", "John Doe",
                "", new Role(2, "customer")));

        var missingRole = new ObjectMapper().writeValueAsString(new User("name@domain.com", "John Doe",
                "secret", null));

        // registration will need token
        var invalidRole = new ObjectMapper().writeValueAsString(new User("name@domain.com", "John Doe",
                "secret", new Role(169, "invalid")));
        var driver = new ObjectMapper().writeValueAsString(new User("driver@domain.com", "Driver Doe",
                "secret", new Role(3, "driver")));


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
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(invalidMail)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(blankName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(blankPassword)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(missingRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        authorize("admin@domain.com");

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(invalidRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .content(driver)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());
    }

    @Order(2)
    @Test
    void updateUser() throws Exception {
        var withoutAddress = new ObjectMapper().writeValueAsString(new User("jane.doe@domain.com", "Mrs Jane Doe",
                "secret", new Role(2, "customer")));

        authorize("jane.doe@domain.com");

        //not her account
        Assertions.assertThrows(ServletException.class, () -> mvc.perform(MockMvcRequestBuilders
                .put("/user/1")
                .content(withoutAddress)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)));

        //her account
        mvc.perform(MockMvcRequestBuilders
                        .put("/user/3")
                        .content(withoutAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists());

        var alreadyExist = new ObjectMapper().writeValueAsString(new User("customer@domain.com", "Mrs Jane Doe",
                "secret", new Role(2, "customer")));

        mvc.perform(MockMvcRequestBuilders
                        .put("/user/3")
                        .content(alreadyExist)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        var forbiddenRole = new ObjectMapper().writeValueAsString(new User("jane.doe@domain.com", "Mrs Jane Doe",
                "secret", new Role(1, "admin")));

        mvc.perform(MockMvcRequestBuilders
                        .put("/user/3")
                        .content(forbiddenRole)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Order(3)
    @Test
    void deleteUser() throws Exception {
        authorize("jane.doe@domain.com");

        //not her account
        Assertions.assertThrows(ServletException.class, () -> mvc.perform(MockMvcRequestBuilders
                .delete("/user/1")
                .accept(MediaType.APPLICATION_JSON)));

        //her account
        mvc.perform(MockMvcRequestBuilders
                        .delete("/user/3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void placeOrder() throws Exception {
        com.pizzadelivery.server.data.entities.User user =
                new com.pizzadelivery.server.data.entities.User(9, "john.doe@domain.com",
                        new StreetName(1, "Baker str."), 1);

        var invalid = new ObjectMapper().writeValueAsString(List.of(
                new FoodOrder(user, new Menu(32, "polyJuicePotion"))));
        var missingAddress = new ObjectMapper().writeValueAsString(List.of(
                new FoodOrder(new com.pizzadelivery.server.data.entities.User(9, "john.doe@domain.com"),
                        new Menu(1, "pizza"))));
        var valid = new ObjectMapper().writeValueAsString(List.of(
                new FoodOrder(user, new Menu(1, "pizza"))));

        UserDetails userDetails = userService.loadUserByUsername("john.doe@domain.com");

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        // user can only order for himself
        mvc.perform(MockMvcRequestBuilders
                        .post("/user/2/order")
                        .content(valid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

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

    // user entity would not work for serialization because password field
    static class User {
        private final String email;
        private final String name;
        private final String password;
        private StreetName streetNameByStreetNameId;
        private int houseNo;
        private final Role roleByRoleId;

        public User(String email, String name, String password, Role roleByRoleId) {
            this.email = email;
            this.name = name;
            this.password = password;
            this.roleByRoleId = roleByRoleId;
        }

        public User(String email, String name, String password, StreetName streetNameByStreetNameId, int houseNo, Role roleByRoleId) {
            this.email = email;
            this.name = name;
            this.password = password;
            this.streetNameByStreetNameId = streetNameByStreetNameId;
            this.houseNo = houseNo;
            this.roleByRoleId = roleByRoleId;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }

        public StreetName getStreetNameByStreetNameId() {
            return streetNameByStreetNameId;
        }

        public int getHouseNo() {
            return houseNo;
        }

        public Role getRoleByRoleId() {
            return roleByRoleId;
        }
    }
}