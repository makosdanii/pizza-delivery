package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin
@RestController
@RequestMapping("/order")
@PreAuthorize("hasAuthority('admin')")
public class OrderController {
    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Iterable<FoodOrder> readFoodOrders() {
        return orderService.readOrders();
    }

    @DeleteMapping("/{id}")
    public Iterable<FoodOrder> deleteFoodOrder(@PathVariable int id) {
        var orders = orderService.deleteOrder(id);
        if (!orders.iterator().hasNext()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return orders;
    }

    @GetMapping("/delivery")
    public Iterable<OrderDelivery> readOrderDelivery() {
        return orderService.readDeliveries();
    }
}
