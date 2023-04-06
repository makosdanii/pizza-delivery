package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.repositories.CarRepository;
import com.pizzadelivery.server.data.repositories.InventoryRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class InventoryService implements ServiceORM<Inventory> {
    InventoryRepository inventoryRepository;
    CarRepository carRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, CarRepository carRepository) {
        this.inventoryRepository = inventoryRepository;
        this.carRepository = carRepository;
    }

    public Inventory createInventory(Inventory inventory) throws AlreadyExistsException {
        checkConstraint(inventory, true);
        return inventoryRepository.save(inventory);
    }

    public Iterable<Inventory> readInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    public void checkConstraint(Inventory inventory, boolean notExistYet) throws AlreadyExistsException {
        if (!carRepository.existsById(inventory.getCarByCarId().getId())) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }
}
