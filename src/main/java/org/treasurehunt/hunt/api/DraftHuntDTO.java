package org.treasurehunt.hunt.api;

import org.treasurehunt.common.enums.HuntStatus;

import java.time.Instant;

public record DraftHuntDTO(
        Long id,
        String title,
        String description,
        Long organizerId,
        Long reviewerId,
        Instant startDate,
        Instant endDate,
        HuntStatus huntStatus,
        LocationDTO location
) {
}
