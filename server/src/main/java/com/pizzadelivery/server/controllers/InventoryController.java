package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.services.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasAuthority('admin')")
public class InventoryController extends Controller {
    private InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping()
    public Iterable<Inventory> readInventory() {
        return inventoryService.readInventory();
    }
}
