package com.pizzadelivery.server.data.validation;

import jakarta.validation.groups.Default;

/**
 * Validator group. Belonging validators will not be triggered on the moment of persisting
 */
public interface NonValidatedOnPersistTime extends Default {
}
