package org.treasurehunt.hunt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.common.enums.ChallengeType;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.common.util.FIleUploadUtil;
import org.treasurehunt.common.validation.ValidatorService;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.*;
import org.treasurehunt.hunt.mapper.ChallengeMapper;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.*;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

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
    private final UserRepository userRepository;
    private final SubmissionRepo submissionRepo;


    @Transactional
    public CreateChallengeResponse createChallenge(Long huntId, Long organizerId, String createChallengeDTO, MultipartFile image) throws IOException {
        try {
            // Convert JSON string to CreateChallengeDTO object
            CreateChallengeDTO challengeDTO = objectMapper.readValue(createChallengeDTO, CreateChallengeDTO.class);

            // Validate the DTO
            validatorService.validate(challengeDTO);


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
            if (EnumSet.of(ChallengeType.CODING, ChallengeType.BUGFIX).contains(challengeDTO.getChallengeType()) && (challengeDTO.getChallengeCodes() != null)) {
                // verify the validity
                challengeDTO.getChallengeCodes()
                        .forEach(validatorService::validate);

                List<ChallengeCode> challengeCodes = new ArrayList<>();
                challengeDTO.getChallengeCodes().forEach(
                        (dto -> {
                            ChallengeCode challengeCode = new ChallengeCode();
                            challengeCode.setChallenge(challengeToCreate);
                            challengeCode.setCode(dto.code());
                            challengeCode.setLanguage(dto.language());
                            challengeCodes.add(challengeCode);
                        })
                );
                challengeToCreate.setChallengeCodes(challengeCodes);
            } else if (EnumSet.of(ChallengeType.CODING, ChallengeType.BUGFIX).contains(challengeDTO.getChallengeType())) {
                throw new BadRequestException(challengeDTO.getChallengeType().name() + " Must have at least one challenge code");
            }

            if (challengeDTO.getChallengeType().equals(ChallengeType.BUGFIX)
                && (challengeDTO.getOptimalSolutions() == null)) {
                throw new BadRequestException("Bug fix must have at least one optimal solution code to test");
            } else if (challengeDTO.getChallengeType().equals(ChallengeType.BUGFIX)) {
                challengeDTO.getOptimalSolutions()
                        .forEach(validatorService::validate);
                challengeDTO.getOptimalSolutions()
                        .forEach(sol -> {
                            sol.setChallenge(challengeToCreate);
                        });
                challengeToCreate.setOptimalSolutions(challengeDTO.getOptimalSolutions());
            }

            if (EnumSet.of(ChallengeType.CODING, ChallengeType.BUGFIX).contains(challengeDTO.getChallengeType()) &&
                (challengeDTO.getTestCases() == null || challengeDTO.getTestCases().isEmpty())) {
                throw new BadRequestException(challengeDTO.getChallengeType().name() + " Must have test cases");
            } else if (!challengeDTO.getChallengeType().equals(ChallengeType.GAME)) {
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
     * Retrieves all challenges for a specific hunt
     * Only allows access if the user is an admin or the organizer of the hunt
     *
     * @param huntId      the ID of the hunt
     * @param userId      the ID of the user making the request
     * @param authorities the authorities of the user making the request
     * @return a list of challenge response DTOs
     * @throws EntityNotFoundException if the hunt is not found
     * @throws RuntimeException        if the user is not authorized to access the hunt's challenges
     */
    public List<CreateChallengeResponse> getChallengesByHuntIdWithAuth(Long huntId, Long userId, Collection<? extends GrantedAuthority> authorities) {
        // Verify that the hunt exists
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException(huntId, Hunt.class));

        // Check if user is admin
        boolean isAdmin = false;
        for (GrantedAuthority authority : authorities) {
            if (List.of(Roles.ADMIN.name(), Roles.REVIEWER.name()).contains(authority.getAuthority())) {
                isAdmin = true;
                break;
            }
        }

        // If not admin, check if user is the organizer of the hunt
        if (!isAdmin) {
            User huntOrganizer = hunt.getOrganizer();
            if (!Objects.equals(huntOrganizer.getId(), userId)) {
                throw new RuntimeException("You are not authorized to access this hunt's challenges");
            }
        }

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

    public ChallengeInfo getChallengesInfo(Long huntId) {
        Hunt hunt = huntRepository.findById(huntId)
                .orElseThrow(() -> new EntityNotFoundException(huntId, Hunt.class));

        User user = userRepository.findById(AuthUtil.getUserFromSecurityContext().orElseThrow().getId())
                .orElseThrow(() -> new EntityNotFoundException(huntId, Hunt.class));

        List<Challenge> challenges = hunt.getChallenges();

        List<ChallengeState> challengeStates = new ArrayList<>();
        long total = 0L;
        for (Challenge c : challenges) {
            List<Submission> submissions = submissionRepo.findByChallengeIdAndUserId(c.getId(), user.getId());
            ChallengeState challengeState = new ChallengeState();
            boolean solved = submissions.stream()
                    .anyMatch(s -> s.getStatus().equals(Submission.SubmissionStatus.SUCCESS));
            Long score = submissions.stream()
                    .reduce(0L,
                            (acc, sub) -> sub.getStatus().equals(Submission.SubmissionStatus.FAIL) ? acc - 1L : acc,
                            Long::sum);
            score = solved ? score + c.getPoints() : score;
            challengeState.setChallenge_id(c.getId());
            challengeState.setSolved(solved);
            total = Long.sum(total, score);

            challengeStates.add(challengeState);
        }

        return new ChallengeInfo(total, challengeStates);
    }

    public String getImgPiece(Long id) {
        return challengeRepository.getImageById(id);
    }

    @Transactional
    public void addScoreToGameChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("No such challenge"));

        UserDetailsDTO userDTO = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(() -> new EntityNotFoundException("No user found"));

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("No user found"));

        List<Submission> submissions = submissionRepo.findByChallengeIdAndUserId(
                challenge.getId(), user.getId()
        );

        boolean wonBefore = submissions.stream()
                .anyMatch((submission -> submission.getStatus().equals(Submission.SubmissionStatus.SUCCESS)));

        if (wonBefore) {
            return;
        }

        int challengeScore = challenge.getPoints();

        user.setScore(user.getScore() + challengeScore);

        userRepository.save(user);

        Submission submission = new Submission();
        submission.setChallengeId(challengeId);
        submission.setTime(Instant.now());
        submission.setUserId(user.getId());
        submission.setStatus(Submission.SubmissionStatus.SUCCESS);

        submissionRepo.save(submission);
    }
}
