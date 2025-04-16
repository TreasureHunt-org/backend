package org.treasurehunt.hunt.api;

import org.treasurehunt.common.enums.ChallengeType;

import java.time.Instant;
import java.util.List;

public record CreateChallengeResponse(
        Long challengeId,
        Long huntId,
        String title,
        String description,
        Integer points,
        ChallengeType challengeType,
        String externalGameUri,
        List<TestCaseDTO> testCases,
        Instant createdAt
) {
}
