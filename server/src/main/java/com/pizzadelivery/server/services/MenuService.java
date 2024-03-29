package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Ingredient;
import com.pizzadelivery.server.data.entities.Menu;
import com.pizzadelivery.server.data.entities.MenuIngredient;
import com.pizzadelivery.server.data.entities.MenuIngredientPK;
import com.pizzadelivery.server.data.repositories.IngredientRepository;
import com.pizzadelivery.server.data.repositories.MenuIngredientRepository;
import com.pizzadelivery.server.data.repositories.MenuRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class MenuService extends ServiceORM<Menu> {
    MenuRepository menuRepository;
    IngredientRepository ingredientRepository;
    MenuIngredientRepository menuIngredientRepository;

    @Autowired
    public MenuService(MenuRepository menuRepository, IngredientRepository ingredientRepository, MenuIngredientRepository menuIngredientRepository) {
        this.menuRepository = menuRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuIngredientRepository = menuIngredientRepository;
    }

    public Menu createMenu(Menu menu) throws AlreadyExistsException {
        checkConstraint(menu, true);
        menu.setId(UNASSIGNED);
        return menuRepository.save(menu);
    }

    public Menu assignIngredient(int id, MenuIngredient menuIngredient) {
        Menu menu = menuRepository.findById(id).orElse(new Menu());
        Ingredient ingredient = ingredientRepository
                .findById(menuIngredient.getIngredientByIngredientId().getId()).orElse(new Ingredient());

        if (menu.getId() != UNASSIGNED && ingredient.getId() != UNASSIGNED) {
            menuIngredient.setMenuByMenuId(menu);
            menuIngredientRepository.save(menuIngredient);
            return menuRepository.findById(id).get();
        }
        menu.setId(UNASSIGNED);
        return menu;
    }

    public Menu unAssignIngredient(int id, int ingredientId) {
        Menu menu = menuRepository.findById(id).orElse(new Menu());
        Ingredient ingredient = ingredientRepository
                .findById(ingredientId).orElse(new Ingredient());

        if (menu.getId() != UNASSIGNED && ingredient.getId() != UNASSIGNED) {
            menuIngredientRepository.deleteById(new MenuIngredientPK(menu, ingredient));
            menu.setMenuIngredientsById(menu.getMenuIngredientsById().stream()
                    .filter(menuIngredient -> menuIngredient.getIngredientByIngredientId().getId() != ingredientId).toList());
            return menu;
        }
        return menu;
    }

    public Menu findMenu(int id) {
        return menuRepository.findById(id).orElse(new Menu());
    }

    public Iterable<Menu> listAll() {
        return menuRepository.findAll();
    }

    public Stream<MenuIngredient> listAllMenuIngredientsByIds(int menuId) {
        return menuRepository.findById(menuId).get().getMenuIngredientsById().stream();
    }

    public Menu updateMenu(int id, Menu menu) throws AlreadyExistsException {
        Menu old = menuRepository.findById(id).orElse(new Menu());
        if (old.getId() != UNASSIGNED) {
            menu.setId(id);
            checkConstraint(menu, !old.getName().equals(menu.getName()));
            return menuRepository.save(menu);
        }
        return old;
    }

    public boolean deleteMenu(int id) {
        if (!menuRepository.existsById(id)) {
            return false;
        }

        menuRepository.deleteById(id);
        return true;
    }

    @Override
    public void checkConstraint(Menu menu, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !menuRepository.findByName(menu.getName()).isEmpty()) {
            throw new AlreadyExistsException();
        }
    }
}
