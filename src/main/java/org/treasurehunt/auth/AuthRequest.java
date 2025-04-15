package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthRequest(
        @Email(message = "Please provide a valid email address.")
        @NotBlank(message = "Email is required.")
        String email,

        @NotBlank(message = "Password is required.")
        String password) {
}