package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {
    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyAuthority('admin', 'customer')")
    @GetMapping
    public Iterable<FoodOrder> readFoodOrders(@RequestParam(required = false) Date before) {
        var isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("admin"));
        return orderService.readOrders(before, isAdmin);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    public Iterable<FoodOrder> deleteFoodOrder(@PathVariable int id) {
        var orders = orderService.deleteOrder(id);
        if (!orders.iterator().hasNext()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return orders;
    }
}
