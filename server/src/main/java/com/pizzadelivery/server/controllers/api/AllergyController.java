package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.controllers.Controller;
import com.pizzadelivery.server.data.entities.Allergy;
import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import com.pizzadelivery.server.services.AllergyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

@Validated
@RestController
@CrossOrigin
@RequestMapping("/allergy")
@PreAuthorize("hasAnyAuthority('admin', 'chef')")
public class AllergyController extends Controller {
    private final AllergyService allergyService;

    @Autowired
    public AllergyController(AllergyService allergyService) {
        this.allergyService = allergyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Allergy> findAllergy(@PathVariable @Positive int id) {
        Allergy allergy = allergyService.findAllergy(id);
        return new ResponseEntity<>(allergy, allergy.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @GetMapping
    public Iterable<Allergy> listAllergy() {
        return allergyService.listAll();
    }

    @PostMapping
    public ResponseEntity<Allergy> registerAllergy(@RequestBody @Valid Allergy allergy) throws AlreadyExistsException {
        return new ResponseEntity<>(allergyService.createAllergy(allergy), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Allergy> updateAllergy(@PathVariable @Positive int id, @RequestBody @Valid Allergy allergy) throws AlreadyExistsException {
        allergy = allergyService.updateAllergy(id, allergy);
        return new ResponseEntity<>(allergy, allergy.getId() == UNASSIGNED ? HttpStatus.NOT_FOUND : HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAllergy(@PathVariable @Positive int id) {
        return new ResponseEntity<>("", allergyService.deleteAllergy(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
