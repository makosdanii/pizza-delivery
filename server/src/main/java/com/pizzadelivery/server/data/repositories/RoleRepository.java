package com.pizzadelivery.server.data.repositories;

import com.pizzadelivery.server.data.entities.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
    List<Role> findByName(String name);
}
