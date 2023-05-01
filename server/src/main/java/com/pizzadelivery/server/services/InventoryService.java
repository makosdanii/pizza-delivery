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

import java.util.HashSet;
import java.util.List;

import static com.pizzadelivery.server.utils.Dispatcher.INVENTORY_CAPACITY;
import static java.lang.Thread.sleep;

@Service
public class InventoryService implements ServiceORM<Inventory> {
    InventoryRepository inventoryRepository;
    CarRepository carRepository;
    IngredientRepository ingredientRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, CarRepository carRepository, IngredientRepository ingredientRepository) {
        this.inventoryRepository = inventoryRepository;
        this.carRepository = carRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Iterable<Inventory> readInventory() {
        return inventoryRepository.findAll();
    }

    public int readInventoryLatestQt(Ingredient ingredient) {
        var inventory = inventoryRepository
                .findByIngredientByIngredientId(ingredient,
                        Sort.by(Sort.Direction.DESC, "id.modifiedAt"));
        return inventory.isEmpty() ? 0 : inventory.get(0).getQuantity();
    }

    public void fillInventory() {
        System.out.println("----------Filling inventory----------");
        ingredientRepository.findAll().forEach(ingredient -> {
            var current = readInventoryLatestQt(ingredient);
            var deficit = INVENTORY_CAPACITY - current;
            if (deficit > 0)
                modifyInventory(new Inventory(100, deficit, ingredient), true); //TODO varied price

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("----------Inventory filled----------");
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
                    .findByIngredientByIngredientId(inventory.getIngredientByIngredientId(),
                            Sort.by(Sort.Direction.DESC, "id.modifiedAt"))).isEmpty())
                lastQuantity = records.get(0).getQuantity();


            if (!increase && lastQuantity < inventory.getQuantity())
                throw new ConstraintViolationException("Calculated current quantity cannot be negative", new HashSet<>());

            inventory.setQuantity(increase ?
                    lastQuantity + inventory.getQuantity() :
                    lastQuantity - inventory.getQuantity());

            return inventoryRepository.save(inventory);
        }

        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
    }

    public Iterable<Inventory> deleteInventory(InventoryPK id) {
        var inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid ID constraint", new HashSet<>()));
        if (!inventoryRepository
                .findByIngredientByIngredientId(inventory.getIngredientByIngredientId(),
                        Sort.by(Sort.Direction.DESC, "id.modifiedAt")).get(0).getModifiedAt()
                .equals(inventory.getModifiedAt()))
            throw new ConstraintViolationException("Only the latest record can be deleted per ingredient", new HashSet<>());

        inventoryRepository.deleteById(id);
        return readInventory();
    }

    @Override
    public void checkConstraint(Inventory inventory, boolean notExistYet) throws AlreadyExistsException {

    }
}
