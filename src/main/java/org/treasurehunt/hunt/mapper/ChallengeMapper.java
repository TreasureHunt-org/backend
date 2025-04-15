package org.treasurehunt.hunt.mapper;

import org.mapstruct.Mapper;
import org.treasurehunt.hunt.api.ChallengeCodeRequest;
import org.treasurehunt.hunt.api.CreateChallengeDTO;
import org.treasurehunt.hunt.api.TestCaseDTO;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.ChallengeCode;
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
