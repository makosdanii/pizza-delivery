package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@CrossOrigin
@RestController
@RequestMapping("/delivery")
public class DeliveryController {
    OrderService orderService;

    @Autowired
    public DeliveryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAuthority('admin')")
    @GetMapping
    public Iterable<OrderDelivery> readOrderDelivery(@RequestParam(required = false) Date before) {
        return orderService.readDeliveries(before);
    }
}
