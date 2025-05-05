package org.treasurehunt.hunt.api;

import org.treasurehunt.common.enums.ChallengeType;
import org.treasurehunt.hunt.repository.entity.ChallengeCode;
import org.treasurehunt.hunt.repository.entity.OptimalSolution;

import java.time.Instant;
import java.util.List;

public record CreateChallengeResponse(
        Long challengeId,
//        Long huntId,
        String title,
        String description,
        Integer points,
        ChallengeType challengeType,
        String externalGameUri,
        List<TestCaseDTO> testCases,
        List<OptimalSolutionDTO> optimalSolutions,
        List<ChallengeCodeDTO> challengeCodes,
        Instant createdAt
) {
}
