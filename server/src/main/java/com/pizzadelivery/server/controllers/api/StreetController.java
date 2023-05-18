package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.data.entities.StreetName;
import com.pizzadelivery.server.data.repositories.StreetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/street")
public class StreetController extends Controller {
    private StreetRepository streetRepository;

    @Autowired
    public StreetController(StreetRepository streetRepository) {
        this.streetRepository = streetRepository;
    }

    @GetMapping
    public Iterable<StreetName> listStreets() {
        return streetRepository.findAll();
    }
}
