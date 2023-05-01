package com.pizzadelivery.server.services;


import com.pizzadelivery.server.config.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.FoodOrder;
import com.pizzadelivery.server.data.entities.Role;
import com.pizzadelivery.server.data.entities.StreetName;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.repositories.*;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.utils.Dispatcher;
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
import java.util.Optional;

@Service
public class UserService implements UserDetailsService, ServiceORM<User> {
    UserRepository userRepository;
    StreetRepository streetRepository;
    RoleRepository roleRepository;
    MenuRepository menuRepository;
    FoodOrderRepository foodOrderRepository;
    Dispatcher dispatcher;

    @Autowired
    public UserService(UserRepository userRepository,
                       StreetRepository streetRepository,
                       RoleRepository roleRepository,
                       MenuRepository menuRepository,
                       FoodOrderRepository foodOrderRepository,
                       Dispatcher dispatcher
    ) {
        this.userRepository = userRepository;
        this.streetRepository = streetRepository;
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
        this.foodOrderRepository = foodOrderRepository;
        this.dispatcher = dispatcher;
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
        user.setId(UNASSIGNED);
        user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        return userRepository.save(user);
    }

    public User findUser(int id) {
        return userRepository.findById(id).orElse(new User());
    }

    public Iterable<User> listAll() {
        return userRepository.findAll();
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

    public Integer placeOrder(int id, List<FoodOrder> foodOrders) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ConstraintViolationException("Invalid user id", new HashSet<>()));

        //userByUserId already validated, now check inside
        user.setStreetNameByStreetNameId(Optional
                .ofNullable(foodOrders.get(0).getUserByUserId().getStreetNameByStreetNameId())
                .orElseThrow(() -> new ConstraintViolationException("Missing address", new HashSet<>())));
        user.setHouseNo(Optional.of(foodOrders.get(0).getUserByUserId().getHouseNo())
                .orElseThrow(() -> new ConstraintViolationException("Missing address", new HashSet<>())));
        try {
            checkConstraint(user, false);
        } catch (AlreadyExistsException e) {
            throw new RuntimeException(e);
        }

        foodOrders.forEach(foodOrder -> {
            if (!menuRepository.existsById(foodOrder.getMenuByMenuId().getId())) {
                throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
            } else {
                foodOrder.setUserByUserId(user);
                foodOrderRepository.save(foodOrder);
            }

        });
        try {
            return dispatcher.dispatch(foodOrders);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkConstraint(User user, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !userRepository.findByEmail(user.getEmail()).isEmpty()) {
            throw new AlreadyExistsException();
        }

        if (!roleRepository.existsById(user.getRoleByRoleId().getId())) {
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }

        //if there's no authenticated admin user then only customer user can be created
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                !SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            Role customer = null;
            try {
                customer = roleRepository.findByName("customer").get(0);
            } catch (IndexOutOfBoundsException e) {
                throw new ConstraintViolationException("Necessary role is yet to be created", new HashSet<>());
            }

            if (user.getRoleByRoleId().getId() != customer.getId()) {
                throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
            }
        }

        if (user.getStreetNameByStreetNameId() != null) {
            var street = streetRepository.findById(user.getStreetNameByStreetNameId().getId()).orElse(new StreetName());
            if (street.getId() == UNASSIGNED) {
                throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
            } else if (user.getHouseNo() > street.getUntilNo()) {
                throw new ConstraintViolationException("Invalid address", new HashSet<>());
            }
        }
    }

}
