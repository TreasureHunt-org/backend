package org.treasurehunt.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record UserAuthResponse(
        @JsonProperty String id,
        @JsonProperty String username,
        @JsonProperty String email,
        @JsonProperty String[] roles,
        @JsonProperty String refreshToken,
        @JsonProperty String accessToken

) {
}