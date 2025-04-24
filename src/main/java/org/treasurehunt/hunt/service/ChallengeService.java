package org.treasurehunt.hunt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.common.enums.ChallengeType;
import org.treasurehunt.common.util.FIleUploadUtil;
import org.treasurehunt.common.validation.ValidatorService;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.CreateChallengeDTO;
import org.treasurehunt.hunt.api.CreateChallengeResponse;
import org.treasurehunt.hunt.api.SubmitSolutionRequest;
import org.treasurehunt.hunt.api.SubmitSolutionResponse;
import org.treasurehunt.hunt.mapper.ChallengeMapper;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.ChallengeCode;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.repository.entity.TestCase;
import org.treasurehunt.user.repository.entity.User;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.treasurehunt.common.constants.UploadingConstants.ALLOWED_TYPES;
import static org.treasurehunt.common.constants.UploadingConstants.CHALLENGE_PIECES_UPLOAD_DIR;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final HuntRepository huntRepository;
    private final ChallengeMapper challengeMapper;
    private final ObjectMapper objectMapper;
    private final Judge0Service judge0Service;
    private final ValidatorService validatorService;


    @Transactional
    public CreateChallengeResponse createChallenge(Long huntId, Long organizerId, String createChallengeDTO, MultipartFile image) throws IOException {
        try {
            // Convert JSON string to CreateChallengeDTO object
            CreateChallengeDTO challengeDTO = objectMapper.readValue(createChallengeDTO, CreateChallengeDTO.class);

            // Validate the DTO
            validatorService.validate(challengeDTO);

            if (challengeDTO.getChallengeType() == ChallengeType.CODING) {
                validatorService.validate(challengeDTO.getChallengeCode());
            }

            // Step 1: Get the hunt by ID first
            Hunt huntById = huntRepository.findById(huntId)
                    .orElseThrow(() -> new EntityNotFoundException(huntId, Hunt.class));

            // Step 2: Validate the organizer
            User huntOrganizer = huntById.getOrganizer();
            if (!Objects.equals(huntOrganizer.getId(), organizerId)) {
                throw new RuntimeException("Hunt id doesn't belong to you");
            }

            // Step 3: Map the DTO to the Challenge entity
            Challenge challengeToCreate = challengeMapper.toChallenge(challengeDTO);
            challengeToCreate.setHunt(huntById);
            challengeToCreate.setCreatedAt(Instant.now());
            challengeToCreate.setUpdatedAt(Instant.now());

            // Step 4: Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                if (!ALLOWED_TYPES.contains(image.getContentType())) {
                    throw new BadRequestException("Only " + String.join(" and ", ALLOWED_TYPES) + " images are allowed.");
                }

                String fileExtension = Objects.equals(image.getContentType(), "image/png") ? ".png" : ".jpg";
                String imageName = "challenge_" + huntId + "_" + System.currentTimeMillis() + fileExtension;
                FIleUploadUtil.saveFile(CHALLENGE_PIECES_UPLOAD_DIR, imageName, image);
                challengeToCreate.setMapPieceUri(imageName);
            }

            // Step 5: Handle ChallengeCode if provided
            if (challengeDTO.getChallengeCode() != null) {
                ChallengeCode challengeCode = new ChallengeCode();
                challengeCode.setChallenge(challengeToCreate);
                challengeCode.setCode(challengeDTO.getChallengeCode().code());
                challengeCode.setLanguage(challengeDTO.getChallengeCode().language());
                challengeToCreate.setChallengeCodes(List.of(challengeCode)); // Use a list to set the ChallengeCodes
            }

            // Step 6: Handle TestCases if provided, and the challenge type is not 'GAME'
            if (challengeDTO.getTestCases() != null && !challengeDTO.getTestCases().isEmpty()
                && challengeDTO.getChallengeType() != ChallengeType.GAME) {
                List<TestCase> testCases = challengeDTO.getTestCases().stream()
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

            // Step 7: Save the challenge to the database
            return challengeMapper.fromEntity(challengeRepository.save(challengeToCreate));
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Failed to process Challenge JSON: " + ex.getMessage());
        }
    }

    /**
     * Retrieves a challenge by its ID
     *
     * @param challengeId the ID of the challenge to retrieve
     * @return the challenge response DTO
     * @throws EntityNotFoundException if the challenge is not found
     */
    public CreateChallengeResponse getChallengeById(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException(challengeId, Challenge.class));

        return challengeMapper.fromEntity(challenge);
    }

    /**
     * Deletes a challenge by its ID and removes its associated image file
     *
     * @param challengeId the ID of the challenge to delete
     * @throws EntityNotFoundException if the challenge is not found
     */
    @Transactional
    public void deleteChallenge(Long challengeId) {
        log.info("Deleting challenge with ID: {}", challengeId);

        // Find the challenge by ID
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException(challengeId, Challenge.class));

        // Get the image filename
        String mapPieceUri = challenge.getMapPieceUri();

        // Delete the challenge from the database
        challengeRepository.delete(challenge);

        // If there's an associated image, delete it
        if (mapPieceUri != null && !mapPieceUri.isEmpty()) {
            boolean imageDeleted = FIleUploadUtil.deleteFile(CHALLENGE_PIECES_UPLOAD_DIR, mapPieceUri);
            if (!imageDeleted) {
                log.warn("Failed to delete challenge image: {}", mapPieceUri);
            } else {
                log.info("Successfully deleted challenge image: {}", mapPieceUri);
            }
        }

        log.info("Challenge with ID: {} successfully deleted", challengeId);
    }

    /**
     * Retrieves all challenges for a specific hunt
     *
     * @param huntId the ID of the hunt
     * @return a list of challenge response DTOs
     */
    public List<CreateChallengeResponse> getChallengesByHuntId(Long huntId) {
        // Verify that the hunt exists
        huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException(huntId, Hunt.class));

        // Get all challenges for the hunt
        List<Challenge> challenges = challengeRepository.findByHuntId(huntId);

        // Map the challenges to DTOs and return
        return challenges.stream()
                .map(challengeMapper::fromEntity)
                .toList();
    }

    /**
     * Submits a solution to a challenge and validates it using Judge0
     *
     * @param userId  the ID of the user submitting the solution
     * @param request the solution submission request
     * @return the result of the submission
     */
    @Transactional
    public SubmitSolutionResponse submitSolution(Long userId, SubmitSolutionRequest request) {
        log.info("User {} submitting solution for challenge {}", userId, request.challengeId());

        // Get the challenge
        Challenge challenge = challengeRepository.findById(request.challengeId())
                .orElseThrow(() -> new EntityNotFoundException(request.challengeId(), Challenge.class));

        // Verify that the challenge type is CODING or BUGFIX
        if (challenge.getChallengeType() != ChallengeType.CODING &&
            challenge.getChallengeType() != ChallengeType.BUGFIX) {
            throw new BadRequestException("Challenge type must be CODING or BUGFIX for code submission");
        }

        // Get the test cases
        List<TestCase> testCases = challenge.getTestCases();
        if (testCases == null || testCases.isEmpty()) {
            throw new BadRequestException("Challenge has no test cases");
        }

        // Validate the code using Judge0
        List<SubmitSolutionResponse.TestCaseResult> testCaseResults =
                judge0Service.validateCode(request.sourceCode(), request.language(), testCases);

        // Check if all test cases passed
        boolean allPassed = testCaseResults.stream().allMatch(SubmitSolutionResponse.TestCaseResult::passed);

        String message = allPassed ?
                "All test cases passed! You've completed the challenge." :
                "Some test cases failed. Please check the results and try again.";

        return new org.treasurehunt.hunt.api.SubmitSolutionResponse(
                challenge.getId(),
                allPassed,
                message,
                testCaseResults
        );
    }
}
