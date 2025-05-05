package org.treasurehunt.hunt.api;

import jakarta.validation.constraints.Future;

import java.time.Instant;

public record HuntUpdateRequest(
        @Future
        Instant startDate,
        @Future
        Instant endDate
) {
}
