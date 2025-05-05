package org.treasurehunt.hunt.api;

import org.treasurehunt.common.enums.SupportedLanguages;

public record OptimalSolutionDTO(
        Long id,
        String code,
        SupportedLanguages language
) {}

