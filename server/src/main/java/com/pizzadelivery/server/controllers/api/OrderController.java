package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.controllers.Controller;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.validation.NonValidatedOnPersistTime;
import com.pizzadelivery.server.services.OrderService;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController extends Controller {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyAuthority('admin', 'customer')")
    @GetMapping
    public Iterable<FoodOrder> readFoodOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date before) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("admin"));
        return orderService.readOrders(before, isAdmin);
    }


    @PreAuthorize("principal.getId() == #id")
    @PostMapping("/{id}")
    public ResponseEntity<Integer> placeOrder(@PathVariable @Positive @P("id") int id,
                                              @RequestBody @Validated(NonValidatedOnPersistTime.class) List<FoodOrder> foodOrders) {
        int order = orderService.placeOrder(id, foodOrders);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public Iterable<FoodOrder> deleteFoodOrder(@PathVariable int id) {
        Iterable<FoodOrder> orders = orderService.deleteOrder(id);
        if (!orders.iterator().hasNext()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return orders;
    }
}
