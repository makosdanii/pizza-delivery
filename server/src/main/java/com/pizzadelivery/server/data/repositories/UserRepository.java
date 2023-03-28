package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    public List<User> findByEmail(String email);
}
