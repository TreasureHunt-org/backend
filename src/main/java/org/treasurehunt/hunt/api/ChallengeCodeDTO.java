package org.treasurehunt.hunt.api;

import org.treasurehunt.common.enums.SupportedLanguages;

public record ChallengeCodeDTO(
        Long id,
        String code,
        SupportedLanguages language
) {}
