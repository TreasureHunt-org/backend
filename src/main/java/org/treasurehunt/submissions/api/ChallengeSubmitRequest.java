package org.treasurehunt.submissions.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.treasurehunt.common.enums.SupportedLanguages;

public record ChallengeSubmitRequest(
        @NotNull
        SupportedLanguages language,
        @NotBlank
        String code
) {
}
