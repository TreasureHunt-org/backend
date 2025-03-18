package org.treasurehunt.user.api;

import org.treasurehunt.common.enums.Roles;

import java.time.Instant;

public record UserDto(
        Long id,
        String username,
        String email,
        Roles[] roles,
        Integer score,
        String profilePicture,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
