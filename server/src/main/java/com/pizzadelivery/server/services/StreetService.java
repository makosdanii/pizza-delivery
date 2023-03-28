package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.StreetName;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.data.repositories.StreetRepository;
import com.pizzadelivery.server.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StreetService {
    @Autowired
    StreetRepository streetRepository;

    public StreetService(StreetRepository streetRepository) {
        this.streetRepository = streetRepository;
    }
    public void login() { }

    public void registerStreet(StreetName street) throws Exception {
        if (streetRepository.findByThat(street.getThat()).isEmpty()) {
            streetRepository.save(street);
        }

        throw new Exception("Already registered email address");
    }

    public StreetName findStreet(int id) { return streetRepository.findById(id).orElse(new StreetName()); }
    public boolean updateStreet() { return false; }
    public boolean deleteStreet() { return false; }
}
