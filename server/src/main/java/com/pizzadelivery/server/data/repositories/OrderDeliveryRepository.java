package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.entities.OrderDeliveryPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDeliveryRepository extends CrudRepository<OrderDelivery, OrderDeliveryPK> {
}
