package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.controllers.Controller;
import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.repositories.InventoryRepository;
import com.pizzadelivery.server.services.InventoryService;
import com.pizzadelivery.server.utils.Dispatcher;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
    public Iterable<Inventory> readInventory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date before) {
        return inventoryService.readInventory(before);
    }

    @PostMapping
    public ResponseEntity<Inventory> modifyInventory(@RequestBody @Valid Inventory inventory) {
        inventory = inventoryService.modifyInventory(inventory, inventory.getExpense() > 0);
        if (inventory != null) inventoryService.saveInTransaction(inventory);
        return new ResponseEntity<>(inventory, inventory != null ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    //does not require a whole composite key since only the latest record can be deleted per ingredient
    @DeleteMapping("/{ingredientId}")
    public Iterable<Inventory> deleteInventory(@PathVariable @Positive int ingredientId) {
        return inventoryService.deleteInventory(ingredientId);
    }

    @GetMapping("/reload")
    public int reload() {
        return dispatcher.reset();
    }
}
