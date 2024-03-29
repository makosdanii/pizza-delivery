package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.controllers.Controller;
import com.pizzadelivery.server.data.entities.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.*;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Accepts GET request, with authorization Header. Its only purpose is to fill up DB with test data.
 * So it should only be called once after DB initialization.
 */
@RestController
@CrossOrigin
@RequestMapping("/fill-data")
@PreAuthorize("hasAuthority('admin')")
public class FillController extends Controller {
    private final UserService userService;
    private final RoleService roleService;
    private final CarService carService;
    private final AllergyService allergyService;
    private final IngredientService ingredientService;
    private final MenuService menuService;

    @Autowired
    public FillController(UserService userService,
                          RoleService roleService,
                          CarService carService,
                          AllergyService allergyService,
                          IngredientService ingredientService,
                          MenuService menuService) {
        this.userService = userService;
        this.roleService = roleService;
        this.carService = carService;
        this.allergyService = allergyService;
        this.ingredientService = ingredientService;
        this.menuService = menuService;
    }

    @GetMapping("/basic")
    public User basic() throws AlreadyExistsException {
        User user = new User("john.smith@example.com", "John Smith", "verysecret", new Role(1, "admin"));
        userService.createUser(user);
        return user;
    }

    @GetMapping("/roles")
    public List<Role> roles() throws AlreadyExistsException {
        var data = List.of(new Role("customer"),
                new Role("driver"),
                new Role("chef"));
        for (var entity : data) {
            roleService.createRole(entity);
        }
        return data;
    }

    @GetMapping("/users")
    public List<User> users() throws AlreadyExistsException {
        var data = List.of(new User("john.smith@example.com", "John Smith", "verysecret", new Role(1, "admin")),
                new User("emily.johnson@example.com", "Emily Johnson", "verysecret", new Role(3, "driver")),
                new User("david.williams@example.com", "David Williams", "verysecret", new Role(3, "driver")),
                new User("sarah.davis@example.com", "Sarah Davis", "verysecret", new Role(3, "driver")),
                new User("michael.brown@example.com", "Michael Brown", "verysecret", new Role(3, "driver")),
                new User("jennifer.anderson@example.com", "Jennifer Anderson", "verysecret", new Role(4, "chef")),
                new User("christopher.wilson@example.com", "Christopher Wilson", "verysecret", new Role(2, "customer")),
                new User("daniel.martinez@example.com", "Daniel Martinez", "verysecret", new Role(2, "customer")),
                new User("amanda.thomas@example.com", "Amanda Thomas", "verysecret", new StreetName(1, "Arany"), 1, new Role(2, "customer")),
                new User("jessica.taylor@example.com", "Jessica Taylor", "verysecret", new StreetName(3, "Petofi"), 1, new Role(2, "customer")));
        for (var entity : data) {
            userService.createUser(entity);
        }
        return data;
    }

    @GetMapping("/cars")
    public List<Car> cars() throws AlreadyExistsException {
        var data = List.of(new Car("hsd-675"), new Car("esb-747"), new Car("sle-723"), new Car("nrb-236"));
        for (var entity : data) {
            carService.createCar(entity);
        }
        return data;
    }

    @GetMapping("/allergies")
    public List<Allergy> allergies() throws AlreadyExistsException {
        var data = List.of(new Allergy("Milk"), new Allergy("Peanuts"), new Allergy("Soy"), new Allergy("Wheat"), new Allergy("Fish"));
        for (var entity : data) {
            allergyService.createAllergy(entity);
        }
        return data;
    }

    @GetMapping("/ingredients")
    public List<Ingredient> ingredients() throws AlreadyExistsException {
        var data = List.of(
                new Ingredient("Pizza Dough", 10, new Allergy(4, "Wheat")),
                new Ingredient("Tomato Sauce", 20),
                new Ingredient("Mozzarella Cheese", 40, new Allergy(1, "Milk")),
                new Ingredient("Pepperoni", 50),
                new Ingredient("Mushrooms", 35),
                new Ingredient("Bell Peppers", 35),
                new Ingredient("Onions", 10),
                new Ingredient("Olives", 40),
                new Ingredient("Fresh Basil", 50),
                new Ingredient("Ham", 60),
                new Ingredient("Pineapple", 50),
                new Ingredient("Spinach", 30),
                new Ingredient("Anchovies", 70, new Allergy(5, "Fish")),
                new Ingredient("fuel", 100));
        for (var entity : data) {
            ingredientService.createIngredient(entity);
        }
        return data;
    }

    @GetMapping("/menus")
    public List<Menu> menus() throws AlreadyExistsException {
        var data = List.of(new Menu("Margherita Pizza", 200),
                new Menu("Pepperoni Pizza", 300),
                new Menu("Hawaiian Pizza", 250),
                new Menu("BBQ Chicken Pizza", 350),
                new Menu("Veggie Supreme Pizza", 200),
                new Menu("Four Cheese Pizza", 300),
                new Menu("Mediterranean Pizza", 300));
        for (var entity : data) {
            menuService.createMenu(entity);
        }
        return data;
    }

    @GetMapping("/menu-ingredients")
    public List<MenuIngredient> menuIngredients() {
        var data = List.of(
                new Pair<>(1, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(1, new MenuIngredient(new Ingredient(2, "sauce"), 3)),
                new Pair<>(1, new MenuIngredient(new Ingredient(3, "cheese"), 2)),
                new Pair<>(1, new MenuIngredient(new Ingredient(9, "basil"), 1)),
                new Pair<>(2, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(2, new MenuIngredient(new Ingredient(4, "pepperoni"), 3)),
                new Pair<>(3, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(3, new MenuIngredient(new Ingredient(11, "pineapple"), 5)),
                new Pair<>(4, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(4, new MenuIngredient(new Ingredient(10, "ham"), 4)),
                new Pair<>(5, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(5, new MenuIngredient(new Ingredient(5, "dough"), 3)),
                new Pair<>(5, new MenuIngredient(new Ingredient(6, "dough"), 3)),
                new Pair<>(5, new MenuIngredient(new Ingredient(7, "dough"), 3)),
                new Pair<>(6, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(6, new MenuIngredient(new Ingredient(3, "cheese"), 6)),
                new Pair<>(7, new MenuIngredient(new Ingredient(1, "dough"), 10)),
                new Pair<>(7, new MenuIngredient(new Ingredient(8, "olives"), 5)),
                new Pair<>(7, new MenuIngredient(new Ingredient(13, "fish"), 1))
        );
        data.forEach(entity ->
                menuService.assignIngredient(entity.getValue0(), entity.getValue1())
        );
        return data.stream().map(Pair::getValue1).toList();
    }
}
