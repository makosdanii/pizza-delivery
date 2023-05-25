package com.pizzadelivery.server.controllers.api;

import com.pizzadelivery.server.exceptions.AlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base of controller classes, if extended then any
 *
 * @{ConstraintViolation},
 * @{MethodArgumentNotValid},
 * @{AlreadyExistsException} thrown by controller method will be handled, resulting BAD REQUEST
 */
@org.springframework.stereotype.Controller
public class Controller {
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class,
            org.hibernate.exception.ConstraintViolationException.class, AlreadyExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>("Invalid request: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
