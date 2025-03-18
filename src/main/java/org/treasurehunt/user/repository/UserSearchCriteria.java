package org.treasurehunt.user.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;

public record UserSearchCriteria(
        @JsonProperty
        String username,

        @JsonProperty
        @Email
        String email
) {
}
