package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @JsonProperty("old-password")
        @NotBlank
        String oldPassword,

        @JsonProperty("new-password")
        @NotBlank
        @Size(min = 8, max = 25, message = "Password minimum length is 8")
        String newPassword
) {
}