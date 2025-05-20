package org.treasurehunt.user.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.treasurehunt.auth.CreateUserRequest;
import org.treasurehunt.common.enums.Roles;

import java.util.Set;

/**
 * Request DTO for admin to create a user with specific roles.
 * Extends the standard CreateUserRequest with additional roles field.
 */
public record AdminCreateUserRequest(
        @JsonProperty
        @NotNull(message = "Username is required.")
        @NotEmpty(message = "Username cannot be empty.")
        String username,

        @JsonProperty
        @NotNull(message = "Email is required.")
        @NotEmpty(message = "Email cannot be empty.")
        String email,

        @JsonProperty
        @NotNull(message = "Password is required.")
        @NotEmpty(message = "Password cannot be empty.")
        String password,

        @JsonProperty
        @NotNull(message = "Roles are required.")
        @NotEmpty(message = "At least one role must be specified.")
        Set<Roles> roles
) {
    /**
     * Converts this AdminCreateUserRequest to a standard CreateUserRequest.
     * This is useful when reusing existing user creation logic.
     *
     * @return A CreateUserRequest with the same username, email, and password.
     */
    public CreateUserRequest toCreateUserRequest() {
        return new CreateUserRequest(username, email, password);
    }
}