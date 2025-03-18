package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @JsonProperty
        @NotBlank(message = "Old password is required.")
        String oldPassword,

        @JsonProperty
        @NotBlank(message = "New password is required.")
        @Size(min = 8, max = 25, message = "New password must be between 8 and 25 characters.")
        String newPassword
) {
}