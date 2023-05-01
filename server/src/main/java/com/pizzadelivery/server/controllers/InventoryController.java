package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.entities.InventoryPK;
import com.pizzadelivery.server.services.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@CrossOrigin
@RequestMapping("/inventory")
@PreAuthorize("hasAuthority('admin')")
public class InventoryController extends Controller {
    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Iterable<Inventory> readInventory() {
        return inventoryService.readInventory();
    }

    @PostMapping
    public Iterable<Inventory> deleteInventory(@RequestBody InventoryPK id) {
        return inventoryService.deleteInventory(id);
    }

    @PostMapping("/inventory")
    public ResponseEntity<Inventory> modifyInventory(@RequestBody @Valid Inventory inventory) {
        return new ResponseEntity<>(inventoryService.modifyInventory(inventory, inventory.getExpense() > 0), HttpStatus.CREATED);
    }
}
