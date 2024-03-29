package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.data.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    List<User> findByEmail(String email);

    List<User> findByRoleByRoleId(Role roleByRoleId);
}
