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
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.CreateChallengeDTO;
import org.treasurehunt.hunt.api.CreateChallengeResponse;
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
import java.util.Optional;

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


    @Transactional
    public CreateChallengeResponse createChallenge(Long huntId, Long organizerId, String createChallengeDTO, MultipartFile image) throws IOException {
        try {
            // Convert JSON string to CreateChallengeDTO object
            CreateChallengeDTO challengeDTO = objectMapper.readValue(createChallengeDTO, CreateChallengeDTO.class);

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
     * @return true if the challenge was successfully deleted, false otherwise
     * @throws EntityNotFoundException if the challenge is not found
     */
    @Transactional
    public boolean deleteChallenge(Long challengeId) {
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
        return true;
    }
}
