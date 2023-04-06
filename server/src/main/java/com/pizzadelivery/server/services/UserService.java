package com.pizzadelivery.server.services;


import com.pizzadelivery.server.config.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.repositories.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService implements UserDetailsService, ServiceORM<User> {
    UserRepository userRepository;
    StreetRepository streetRepository;
    RoleRepository roleRepository;
    MenuRepository menuRepository;
    FoodOrderRepository foodOrderRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       StreetRepository streetRepository,
                       RoleRepository roleRepository,
                       MenuRepository menuRepository,
                       FoodOrderRepository foodOrderRepository) {
        this.userRepository = userRepository;
        this.streetRepository = streetRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.foodOrderRepository = foodOrderRepository;
    }

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(username);

        if (user.isEmpty()) throw new UsernameNotFoundException(username);

        List<GrantedAuthority> authority = List.of(
                new SimpleGrantedAuthority(user.get(0).getRoleByRoleId().getName().toLowerCase()));
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        return new UserAuthorizationDetails(
                user.get(0).getId(),
                user.get(0).getEmail(),
                user.get(0).getPassword(),
                enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
                authority);
    }

    public User createUser(User user) throws AlreadyExistsException {
        checkConstraint(user, true);

        //if there's no authenticated admin user then only customer user can be created
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            user.setRoleByRoleId(roleRepository.findByName("customer").get(0));
        }

        user.setId(UNASSIGNED);
        user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        return userRepository.save(user);
    }

    public User findUser(int id) {
        return userRepository.findById(id).orElse(new User());
    }

    public User updateUser(int id, User user) throws AlreadyExistsException {
        User old = userRepository.findById(id).orElse(new User());
        if (old.getId() != UNASSIGNED) {
            checkConstraint(user, !old.getEmail().equals(user.getEmail()));
            user.setId(id);
            user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
            return userRepository.save(user);
        }
        return old;
    }

    public boolean deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            return false;
        }

        userRepository.deleteById(id);
        return true;
    }

    public FoodOrder placeOrder(int id, FoodOrder foodOrder) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid user id", new HashSet<>()));
        if (menuRepository.existsById(foodOrder.getMenuByMenuId().getId())) {
            foodOrder.setUserByUserId(user);
            return foodOrderRepository.save(foodOrder);
        }
        throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
    }

    @Override
    public void checkConstraint(User user, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new AlreadyExistsException();
        }

        if ((user.getStreetNameByStreetNameId() != null &&
                !streetRepository.existsById(user.getStreetNameByStreetNameId().getId()))
                ||
                !roleRepository.existsById(user.getRoleByRoleId().getId())) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }
    }

}
