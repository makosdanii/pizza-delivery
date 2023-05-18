package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.data.entities.Inventory;
import com.pizzadelivery.server.data.entities.InventoryPK;
import com.pizzadelivery.server.data.repositories.CarRepository;
import com.pizzadelivery.server.data.repositories.IngredientRepository;
import com.pizzadelivery.server.data.repositories.InventoryRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.pizzadelivery.server.utils.Dispatcher.INVENTORY_CAPACITY;

@Service
public class InventoryService extends ServiceORM<Inventory> {
    InventoryRepository inventoryRepository;
    CarRepository carRepository;
    IngredientRepository ingredientRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, CarRepository carRepository, IngredientRepository ingredientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.carRepository = carRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Iterable<Inventory> readInventory(Date before) {
        if (before != null) {
            return inventoryRepository.findAllByIdModifiedAtBefore(before);
        }
        return inventoryRepository.findAll();
    }

    public int readInventoryLatestQt(Ingredient ingredient) {
        var inventory = inventoryRepository
                .findByIdIngredientByIngredientId(ingredient,
                        Sort.by(Sort.Direction.DESC, "id.modifiedAt"));
        return inventory.isEmpty() ? 0 : inventory.get(0).getCurrent();
    }

    // persist after in transactional
    public List<Inventory> fillInventory() {
        List<Inventory> inventories = new ArrayList<>();
        ingredientRepository.findAll().forEach(ingredient -> {
            var current = readInventoryLatestQt(ingredient);
            var deficit = INVENTORY_CAPACITY - current;
            if (deficit > 0)
                inventories.add(modifyInventory(new Inventory(deficit * ingredient.getPrice(), deficit, ingredient),
                        true));
        });

        return inventories;
    }

    public Inventory modifyInventory(Inventory inventory, boolean increase) {
        //if it's null then either authorized admin is calling from client or dispatcher fills up, so the same car is used
        if (inventory.getCarByCarId() == null)
            inventory.setCarByCarId(carRepository.findById(1)
                    .orElseThrow(() -> new ConstraintViolationException("No available car", new HashSet<>())));

        if (ingredientRepository.existsById(inventory.getIngredientByIngredientId().getId())) {
            int lastQuantity = 0;
            List<Inventory> records;

            //check if any record of the ingredient is present
            if (!(records = inventoryRepository
                    .findByIdIngredientByIngredientId(inventory.getIngredientByIngredientId(),
                            Sort.by(Sort.Direction.DESC, "id.modifiedAt"))).isEmpty())
                lastQuantity = records.get(0).getCurrent();

            // if dispatcher tried to access unavailable qt from inventory then corresponding orders will be dismissed
            if (!increase && lastQuantity < inventory.getCurrent())
                throw new ConstraintViolationException("Calculated current quantity cannot be negative", new HashSet<>());

            inventory.setCurrent(increase ?
                    lastQuantity + inventory.getCurrent() :
                    lastQuantity - inventory.getCurrent());

            return inventory;
        }

        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
    }

    public Iterable<Inventory> deleteInventory(InventoryPK id) {
        var inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid ID constraint", new HashSet<>()));
        if (!inventoryRepository
                .findByIdIngredientByIngredientId(inventory.getIngredientByIngredientId(),
                        Sort.by(Sort.Direction.DESC, "id.modifiedAt")).get(0).getModifiedAt()
                .equals(inventory.getModifiedAt()))
            throw new ConstraintViolationException("Only the latest record can be deleted per ingredient", new HashSet<>());

        inventoryRepository.deleteById(id);
        return readInventory(null);
    }

    @Override
    public void checkConstraint(Inventory inventory, boolean notExistYet) throws AlreadyExistsException {

    }
}
