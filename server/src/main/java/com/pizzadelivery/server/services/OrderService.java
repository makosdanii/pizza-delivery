package com.pizzadelivery.server.services;

import com.pizzadelivery.server.config.utils.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.repositories.FoodOrderRepository;
import com.pizzadelivery.server.data.repositories.OrderDeliveryRepository;
import com.pizzadelivery.server.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;

@Service
public class OrderService extends ServiceORM<FoodOrder> {
    FoodOrderRepository foodOrderRepository;
    OrderDeliveryRepository orderDeliveryRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderService(FoodOrderRepository foodOrderRepository, OrderDeliveryRepository orderDeliveryRepository, UserRepository userRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.orderDeliveryRepository = orderDeliveryRepository;
        this.userRepository = userRepository;
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

    }
}
