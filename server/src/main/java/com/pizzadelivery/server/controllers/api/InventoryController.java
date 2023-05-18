package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.entities.InventoryPK;
import com.pizzadelivery.server.data.repositories.InventoryRepository;
import com.pizzadelivery.server.services.InventoryService;
import com.pizzadelivery.server.utils.Dispatcher;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@Validated
@RestController
@CrossOrigin
@RequestMapping("/inventory")
@PreAuthorize("hasAuthority('admin')")
public class InventoryController extends Controller {
    private final InventoryService inventoryService;
    private final InventoryRepository inventoryRepository;
    private final Dispatcher dispatcher;

    @Autowired
    public InventoryController(InventoryService inventoryService, InventoryRepository inventoryRepository, Dispatcher dispatcher) {
        this.inventoryService = inventoryService;
        this.inventoryRepository = inventoryRepository;
        this.dispatcher = dispatcher;
    }

    @GetMapping
    public Iterable<Inventory> readInventory(@RequestParam(required = false) Date before) {
        return inventoryService.readInventory(before);
    }

    @PostMapping("/delete")
    public Iterable<Inventory> deleteInventory(@RequestBody InventoryPK id) {
        return inventoryService.deleteInventory(id);
    }

    @PostMapping
    public ResponseEntity<Inventory> modifyInventory(@RequestBody @Valid Inventory inventory) {
        inventory = inventoryRepository.save(inventoryService.modifyInventory(inventory, inventory.getExpense() > 0));
        return new ResponseEntity<>(inventory, HttpStatus.CREATED);
    }

    @GetMapping("/reload")
    public int reload() {
        return dispatcher.reset();
    }
}
