package org.treasurehunt.hunt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.treasurehunt.common.enums.ChallengeType;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.CreateChallengeDTO;
import org.treasurehunt.hunt.mapper.ChallengeMapper;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.ChallengeCode;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.repository.entity.TestCase;
import org.treasurehunt.user.repository.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final HuntRepository huntRepository;
    private final ChallengeMapper challengeMapper;


    public Challenge createChallenge(Long huntId, Long organizerId, CreateChallengeDTO createChallengeDTO) {
        // Step 1: Get the hunt by ID first
        Hunt huntById = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException(huntId, Hunt.class));

        // Step 2: Validate the organizer
        User huntOrganizer = huntById.getOrganizer();
        if (!Objects.equals(huntOrganizer.getId(), organizerId)) {
            throw new RuntimeException("Hunt id doesn't belong to you");
        }

        // Step 3: Map the DTO to the Challenge entity
        Challenge challengeToCreate = challengeMapper.toChallenge(createChallengeDTO);
        challengeToCreate.setHunt(huntById);
        challengeToCreate.setCreatedAt(Instant.now());
        challengeToCreate.setUpdatedAt(Instant.now());

        // Step 4: Handle ChallengeCode if provided
        if (createChallengeDTO.getChallengeCode() != null) {
            ChallengeCode challengeCode = new ChallengeCode();
            challengeCode.setChallenge(challengeToCreate);
            challengeCode.setCode(createChallengeDTO.getChallengeCode().code());
            challengeCode.setLanguage(createChallengeDTO.getChallengeCode().language());
            challengeToCreate.setChallengeCodes(List.of(challengeCode)); // Use a list to set the ChallengeCodes
        }

        // Step 5: Handle TestCases if provided and the challenge type is not 'GAME'
        if (createChallengeDTO.getTestCases() != null && !createChallengeDTO.getTestCases().isEmpty()
                && createChallengeDTO.getChallengeType() != ChallengeType.GAME) {
            List<TestCase> testCases = createChallengeDTO.getTestCases().stream()
                    .map(dto -> {
                        TestCase testCase = new TestCase();
                        testCase.setChallenge(challengeToCreate);
                        testCase.setInput(dto.getInput());
                        testCase.setExpectedOutput(dto.getExpectedOutput());
                        testCase.setOrder(dto.getOrder());
                        return testCase;
                    }).toList();

            challengeToCreate.setTestCases(testCases);
        }

        // Step 6: Save the challenge to the database
        return challengeRepository.save(challengeToCreate);
    }


}
