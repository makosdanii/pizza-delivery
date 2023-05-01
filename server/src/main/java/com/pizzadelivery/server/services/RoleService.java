package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.data.repositories.RoleRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService implements ServiceORM<Role> {
    RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(Role role) throws AlreadyExistsException {
        checkConstraint(role, true);
        role.setId(UNASSIGNED);
        return roleRepository.save(role);
    }

    public Role findRole(int id) {
        return roleRepository.findById(id).orElse(new Role());
    }

    public Iterable<Role> listAll() {
        return roleRepository.findAll();
    }

    public Role updateRole(int id, Role role) throws AlreadyExistsException {
        Role old = roleRepository.findById(id).orElse(new Role());
        if (old.getId() != UNASSIGNED) {
            checkConstraint(role, !old.getName().equals(role.getName()));
            role.setId(id);
            return roleRepository.save(role);
        }

        return old;
    }

    public boolean deleteRole(int id) {
        if (!roleRepository.existsById(id) || roleRepository.findById(id).get().getName().equals("admin")) {
            return false;
        }

        roleRepository.deleteById(id);
        return true;
    }

    @Override
    public void checkConstraint(Role role, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !roleRepository.findByName(role.getName()).isEmpty()) {
            throw new AlreadyExistsException();
        }
    }
}
