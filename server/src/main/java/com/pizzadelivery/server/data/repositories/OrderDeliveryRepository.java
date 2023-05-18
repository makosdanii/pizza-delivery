package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.OrderDelivery;
import com.pizzadelivery.server.data.entities.OrderDeliveryPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface OrderDeliveryRepository extends CrudRepository<OrderDelivery, OrderDeliveryPK> {
    public List<OrderDelivery> findAll();

    public List<OrderDelivery> findAllByIdDeliveredAtBefore(Date before);
}
