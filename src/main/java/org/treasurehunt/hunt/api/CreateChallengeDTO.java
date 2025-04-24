package org.treasurehunt.hunt.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.treasurehunt.common.enums.ChallengeType;
import org.treasurehunt.hunt.repository.entity.ChallengeCode;
import org.treasurehunt.hunt.repository.entity.OptimalSolution;

import java.util.List;

@Data
public class CreateChallengeDTO {

    @NotNull(message = "Title is required" )
    @Size(max = 255, message = "Title must be at most 255 characters" )
    private String title;

    @NotEmpty(message = "Description to the challenge is required" )
    private String description;

    @NotNull(message = "Points are required" )
    private Integer points;

    @NotNull(message = "Challenge type is required" )
    private ChallengeType challengeType;

    private List<ChallengeCodeRequest> challengeCodes;
    private List<OptimalSolution> optimalSolutions;

    @Size(max = 2083, message = "External game URI must be at most 2083 characters" )
    private String externalGameUri;


    private List<@NotNull TestCaseDTO> testCases;
}
