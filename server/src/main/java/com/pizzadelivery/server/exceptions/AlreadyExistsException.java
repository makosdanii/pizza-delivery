package com.pizzadelivery.server.exceptions;

/**
 * Custom exception to be thrown when an entity would be persisted with property not meeting unique constraint
 */
public class AlreadyExistsException extends Exception {
}
