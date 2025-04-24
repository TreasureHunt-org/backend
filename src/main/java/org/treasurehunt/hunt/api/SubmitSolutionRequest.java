package org.treasurehunt.hunt.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.treasurehunt.common.enums.SupportedLanguages;

/**
 * Request DTO for submitting a solution to a challenge
 */
public record SubmitSolutionRequest(
        @NotNull(message = "Challenge ID is required")
        Long challengeId,
        
        @NotBlank(message = "Source code is required")
        String sourceCode,
        
        @NotNull(message = "Programming language is required")
        SupportedLanguages language
) {
}