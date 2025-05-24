package org.treasurehunt.user.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import org.treasurehunt.common.enums.Roles;

public record UpdateUserRequest(
        @JsonProperty
        String name,

        @JsonProperty
        Roles[] roles,

        @Email
        @JsonProperty
        String email
) {
}
