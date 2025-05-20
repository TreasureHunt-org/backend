package org.treasurehunt.hunt.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import org.treasurehunt.common.enums.HuntStatus;

import java.time.Instant;

/**
 * Request object for admin to update hunt details
 */
@Schema(description = "Request for updating hunt details by admin")
public record AdminHuntUpdateRequest(
        @Schema(description = "Hunt title", example = "Spring Treasure Hunt")
        @Size(max = 255, message = "Title must be less than 255 characters")
        String title,

        @Schema(description = "Hunt description", example = "Find the hidden treasures around the campus")
        String description,

        @Schema(description = "Hunt start date", example = "2023-12-01T09:00:00Z")
        @Future(message = "Start date must be in the future")
        Instant startDate,

        @Schema(description = "Hunt end date", example = "2023-12-15T18:00:00Z")
        @Future(message = "End date must be in the future")
        Instant endDate,

        @Schema(description = "Hunt status", example = "LIVE")
        HuntStatus status,

        @Schema(description = "Reviewer ID", example = "2")
        Long reviewerId
) {
}