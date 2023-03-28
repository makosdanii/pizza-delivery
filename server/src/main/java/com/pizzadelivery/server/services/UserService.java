package com.pizzadelivery.server.services;


import com.pizzadelivery.server.config.UserAuthorizationDetails;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.repositories.RoleRepository;
import com.pizzadelivery.server.data.repositories.StreetRepository;
import com.pizzadelivery.server.data.repositories.UserRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    UserRepository userRepository;
    StreetRepository streetRepository;
    RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, StreetRepository streetRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.streetRepository = streetRepository;
        this.roleRepository = roleRepository;
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
        if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
            if (streetRepository.existsById(user.getStreetNameByStreetNameId().getId()) &&
                    roleRepository.existsById(user.getRoleByRoleId().getId())) {

                user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
                return userRepository.save(user);
            }
            throw new ConstraintViolationException("Invalid ID constraint", new HashSet<>());
        }

        throw new AlreadyExistsException();
    }

    public User findUser(int id) {
        return userRepository.findById(id).orElse(new User());
    }

    public User updateUser(int id, User user) {
        User old = userRepository.findById(id).orElse(new User());
        if (old.getId() == 0) {
            return old;
        }

        user.setId(id);
        user.setPassword(passwordEncoder.encode(user.getPassword().trim()));
        return userRepository.save(user);
    }

    public boolean deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            return false;
        }

        userRepository.deleteById(id);
        return true;
    }

}
