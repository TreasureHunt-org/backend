package org.treasurehunt.auth;

import java.time.Instant;

public record LogoutResponse(
        boolean success,
        String message,
        Instant timestamp) {
}