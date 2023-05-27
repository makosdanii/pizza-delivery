package com.pizzadelivery.server.services;

import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
public abstract class ServiceORM<T> {
    public static final int UNASSIGNED = 0;

    public abstract void checkConstraint(T entity, boolean notExistYet) throws AlreadyExistsException;
}
