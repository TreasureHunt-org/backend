package org.treasurehunt.hunt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.treasurehunt.hunt.api.*;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.ChallengeCode;
import org.treasurehunt.hunt.repository.entity.OptimalSolution;
import org.treasurehunt.hunt.repository.entity.TestCase;

import java.util.List;

@Mapper(componentModel = "spring" )
public abstract class ChallengeMapper {

    public Challenge toChallenge(CreateChallengeDTO dto) {

        return Challenge.builder()
                .challengeType(dto.getChallengeType())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .points(dto.getPoints())
                .externalGameUri(dto.getExternalGameUri())
//                .testCases(toTestCases(dto.getTestCases()))
//                .challengeCodes(fromChallengeCodeRequests(dto.getChallengeCode()))
                .build();
    }

    @Mapping(target = "challengeId", source = "id")
    @Mapping(target = "optimalSolutions", expression = "java(mapOptimalSolutions(challenge.getOptimalSolutions()))")
    @Mapping(target = "challengeCodes", expression = "java(mapChallengeCodes(challenge.getChallengeCodes()))")
    public abstract CreateChallengeResponse fromEntity(Challenge challenge);

    protected List<OptimalSolutionDTO> mapOptimalSolutions(List<OptimalSolution> solutions) {
        if (solutions == null) return null;
        return solutions.stream()
                .map(solution -> new OptimalSolutionDTO(
                        solution.getId(),
                        solution.getCode(),
                        solution.getLanguage()
                ))
                .toList();
    }

    protected List<ChallengeCodeDTO> mapChallengeCodes(List<ChallengeCode> codes) {
        if (codes == null) return null;
        return codes.stream()
                .map(code -> new ChallengeCodeDTO(
                        code.getId(),
                        code.getCode(),
                        code.getLanguage()
                ))
                .toList();
    }


    private List<ChallengeCode> fromChallengeCodeRequests(ChallengeCodeRequest codeRequest) {
        return fromChallengeCodeRequests(List.of(codeRequest));
    }

    private List<ChallengeCode> fromChallengeCodeRequests(List<ChallengeCodeRequest> codeRequests) {
        return codeRequests.stream()
                .map(dto -> {
                    ChallengeCode challengeCode = new ChallengeCode();
                    challengeCode.setCode(dto.code());
                    challengeCode.setLanguage(dto.language());
                    return challengeCode;
                }).toList();
    }

    private List<TestCase> toTestCases(List<TestCaseDTO> testCases) {
        return testCases.stream()
                .map(dto -> {
                    TestCase testCase = new TestCase();
                    testCase.setInput(dto.getInput());
                    testCase.setExpectedOutput(dto.getExpectedOutput());
                    testCase.setOrder(dto.getOrder());
                    return testCase;
                }).toList();
    }

}
