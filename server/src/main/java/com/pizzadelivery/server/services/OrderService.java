package com.pizzadelivery.server.services;

import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.repositories.FoodOrderRepository;
import com.pizzadelivery.server.data.repositories.OrderDeliveryRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class OrderService implements ServiceORM<FoodOrder> {
    FoodOrderRepository foodOrderRepository;
    OrderDeliveryRepository orderDeliveryRepository;

    @Autowired
    public OrderService(FoodOrderRepository foodOrderRepository, OrderDeliveryRepository orderDeliveryRepository) {
        this.foodOrderRepository = foodOrderRepository;
        this.orderDeliveryRepository = orderDeliveryRepository;
    }

    public Iterable<FoodOrder> readOrders() {
        return foodOrderRepository.findAll();
    }

    public Iterable<FoodOrder> deleteOrder(int id) {
        var order = foodOrderRepository.findById(id).orElse(new FoodOrder());
        if (order.getId() != UNASSIGNED) {
            foodOrderRepository.deleteById(id);
            return readOrders();
        }
        return new ArrayList<>();
    }

    public Iterable<OrderDelivery> readDeliveries() {
        return orderDeliveryRepository.findAll();
    }

    @Override
    public void checkConstraint(FoodOrder entity, boolean notExistYet) throws AlreadyExistsException {

    }
}
