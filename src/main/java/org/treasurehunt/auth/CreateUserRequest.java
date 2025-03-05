package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @JsonProperty
        @NotBlank
        String username,

        @JsonProperty
        @NotBlank
        @Email
        String email,

        @JsonProperty
        @NotBlank
        @Size(min = 8, max = 25, message = "Password minimum length is 8")
        String password) {
}