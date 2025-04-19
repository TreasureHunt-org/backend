package org.treasurehunt.hunt.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.treasurehunt.common.enums.HuntStatus;

import java.time.Instant;

/**
 * Filter criteria for Hunt entities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HuntFilter {
    private String title;
    private HuntStatus status;
    private Long organizerId;
    private Instant startDateFrom;
    private Instant startDateTo;
    private Instant endDateFrom;
    private Instant endDateTo;
}