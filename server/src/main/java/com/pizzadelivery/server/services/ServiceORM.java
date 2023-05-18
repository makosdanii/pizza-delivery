package com.pizzadelivery.server.services;

import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public abstract class ServiceORM<T> {
    public static final int UNASSIGNED = 0;
    @PersistenceContext
    protected EntityManager entityManager;

    public abstract void checkConstraint(T entity, boolean notExistYet) throws AlreadyExistsException;

    @Transactional
    public void persist(Object entity) {
        entityManager.persist(entity);
        entityManager.flush();
    }
}
