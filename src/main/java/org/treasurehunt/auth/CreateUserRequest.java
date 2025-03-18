package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @JsonProperty
        @NotBlank(message = "Username is required.")
        String username,

        @JsonProperty
        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        String email,

        @JsonProperty
        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 25, message = "Password must be between 8 and 25 characters.")
        String password
) {
}