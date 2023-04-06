package com.pizzadelivery.server.services;

import com.pizzadelivery.server.exceptions.AlreadyExistsException;

public interface ServiceORM<T> {
    public void checkConstraint(T entity, boolean notExistYet) throws AlreadyExistsException;

    public static final int UNASSIGNED = 0;
}
