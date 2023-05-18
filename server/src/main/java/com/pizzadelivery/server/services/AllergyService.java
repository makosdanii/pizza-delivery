package com.pizzadelivery.server.services;


import com.pizzadelivery.server.data.entities.Allergy;
import com.pizzadelivery.server.data.repositories.AllergyRepository;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AllergyService extends ServiceORM<Allergy> {
    AllergyRepository allergyRepository;

    @Autowired
    public AllergyService(AllergyRepository allergyRepository) {
        this.allergyRepository = allergyRepository;
    }

    public Allergy createAllergy(Allergy allergy) throws AlreadyExistsException {
        checkConstraint(allergy, true);
        allergy.setId(0);
        return allergyRepository.save(allergy);
    }

    public Allergy findAllergy(int id) {
        return allergyRepository.findById(id).orElse(new Allergy());
    }

    public Iterable<Allergy> listAll() {
        return allergyRepository.findAll();
    }

    public Allergy updateAllergy(int id, Allergy allergy) throws AlreadyExistsException {
        Allergy old = allergyRepository.findById(id).orElse(new Allergy());
        if (old.getId() != 0) {
            checkConstraint(allergy, !old.getName().equals(allergy.getName()));
            allergy.setId(id);
            return allergyRepository.save(allergy);
        }
        return old;
    }

    public boolean deleteAllergy(int id) {
        if (!allergyRepository.existsById(id)) {
            return false;
        }

        allergyRepository.deleteById(id);
        return true;
    }

    @Override
    public void checkConstraint(Allergy allergy, boolean notExistYet) throws AlreadyExistsException {
        if (notExistYet && !allergyRepository.findByName(allergy.getName()).isEmpty()) {
            throw new AlreadyExistsException();
        }
    }
}
