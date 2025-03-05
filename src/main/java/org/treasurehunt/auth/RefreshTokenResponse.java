package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshTokenResponse(
        @JsonProperty(value = "refresh_token")
        String refreshToken
) {
}