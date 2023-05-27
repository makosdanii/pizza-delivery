package com.pizzadelivery.server.services;

import com.pizzadelivery.server.config.utils.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.repositories.FoodOrderRepository;
import com.pizzadelivery.server.data.repositories.MenuRepository;
import com.pizzadelivery.server.data.repositories.OrderDeliveryRepository;
import com.pizzadelivery.server.data.repositories.UserRepository;
import com.pizzadelivery.server.utils.Dispatcher;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService extends ServiceORM<FoodOrder> {
    private final FoodOrderRepository foodOrderRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final Dispatcher dispatcher;

    @Autowired
    public OrderService(FoodOrderRepository foodOrderRepository,
                        OrderDeliveryRepository orderDeliveryRepository,
                        UserRepository userRepository,
                        MenuRepository menuRepository,
                        Dispatcher dispatcher) {
        this.foodOrderRepository = foodOrderRepository;
        this.orderDeliveryRepository = orderDeliveryRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.dispatcher = dispatcher;
    }

    public Iterable<FoodOrder> readOrders(Date before, boolean isAdmin) {
        if (before != null && isAdmin) {
            return foodOrderRepository.findAllByOrderedAtBefore(before);
        } else if (isAdmin) {
            return foodOrderRepository.findAll();
        } else {
            UserAuthorizationDetails customer = (UserAuthorizationDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return foodOrderRepository.findAllByUserByUserId(userRepository.findById(customer.getId()).get());
        }
    }

    public Integer placeOrder(int id, List<FoodOrder> foodOrders) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid user id", new HashSet<>()));

        User address = Optional
                .ofNullable(foodOrders.get(0).getUserByUserId())
                .orElseThrow(() -> new ConstraintViolationException("Missing address", new HashSet<>()));
        user.setStreetNameByStreetNameId(Optional.ofNullable(address.getStreetNameByStreetNameId())
                .orElseThrow(() -> new ConstraintViolationException("Missing house number", new HashSet<>())));
        user.setHouseNo(address.getHouseNo());

        foodOrders.forEach(foodOrder -> {
            checkConstraint(foodOrder, true);
            foodOrder.setUserByUserId(user);

            // order simulation runs under admin privilege, no need to store those
            if (!Objects.equals(user.getRoleByRoleId().getName(), "admin"))
                foodOrderRepository.save(foodOrder);
        });

        return dispatcher.dispatch(foodOrders);
    }

    public Iterable<FoodOrder> deleteOrder(int id) {
        FoodOrder order = foodOrderRepository.findById(id).orElse(new FoodOrder());
        if (order.getId() != UNASSIGNED) {
            foodOrderRepository.deleteById(id);
            return readOrders(null, true);
        }
        return new ArrayList<>();
    }

    public Iterable<OrderDelivery> readDeliveries(Date before) {
        if (before != null) {
            return orderDeliveryRepository.findAllByIdDeliveredAtBefore(before);
        }
        return orderDeliveryRepository.findAll();
    }

    @Override
    public void checkConstraint(FoodOrder entity, boolean notExistYet) {
        if (!menuRepository.existsById(entity.getMenuByMenuId().getId())) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }
}
