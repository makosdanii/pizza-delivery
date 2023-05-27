package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.entities.OrderDeliveryPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderDeliveryRepository extends CrudRepository<OrderDelivery, OrderDeliveryPK> {
    List<OrderDelivery> findAll();

    List<OrderDelivery> findAllByIdDeliveredAtBefore(Date before);
}
