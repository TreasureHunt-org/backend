package org.treasurehunt.hunt.api;

import jakarta.validation.constraints.NotNull;
import org.treasurehunt.common.enums.SupportedLanguages;

public record ChallengeCodeRequest(
        @NotNull
        SupportedLanguages language,

        @NotNull
        String code
) {
}
