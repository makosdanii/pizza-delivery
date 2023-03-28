package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.data.repositories.RoleRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(Role role) throws AlreadyExistsException {
        if (roleRepository.findByName(role.getName()).isEmpty()) {
            return roleRepository.save(role);
        }

        throw new AlreadyExistsException();
    }

    public Role findRole(int id) {
        return roleRepository.findById(id).orElse(new Role());
    }

    public Role updateRole(int id, Role role) {
        Role old = roleRepository.findById(id).orElse(new Role());
        if (old.getId() == 0) {
            return old;
        }

        role.setId(id);
        return roleRepository.save(role);
    }

    public boolean deleteRole(int id) {
        if (!roleRepository.existsById(id)) {
            return false;
        }

        roleRepository.deleteById(id);
        return true;
    }
}
