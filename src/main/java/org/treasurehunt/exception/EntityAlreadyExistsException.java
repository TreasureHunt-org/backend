package org.treasurehunt.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String field, Object value, Class<?> entity) {
        super(String.format("The %s with %s %s already exists"
                ,entity.getSimpleName().toLowerCase(), field, value.toString()));
    }
}