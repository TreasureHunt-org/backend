package org.treasurehunt.hunt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.treasurehunt.common.enums.ChallengeType;
import org.treasurehunt.common.util.FIleUploadUtil;
import org.treasurehunt.common.validation.ValidatorService;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.CreateChallengeResponse;
import org.treasurehunt.hunt.mapper.ChallengeMapper;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ChallengeServiceTest {

    private ChallengeService challengeService;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private HuntRepository huntRepository;

    @Mock
    private ChallengeMapper challengeMapper;

    @Mock
    private Judge0Service judge0Service;

    @Mock
    private ValidatorService validatorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepo submissionRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        challengeService = new ChallengeService(
                challengeRepository,
                huntRepository,
                challengeMapper,
                null, // ObjectMapper not needed for these tests
                judge0Service,
                validatorService,
                userRepository,
                submissionRepo
        );
    }

    @Test
    void getChallengeById_WithValidId_ShouldReturnChallenge() {
        // Arrange
        Long challengeId = 1L;
        Challenge challenge = new Challenge();
        challenge.setId(challengeId);
        challenge.setTitle("Test Challenge");
        challenge.setDescription("Test Description");
        challenge.setPoints(100);
        challenge.setChallengeType(ChallengeType.CODING);
        challenge.setCreatedAt(Instant.now());

        CreateChallengeResponse expectedResponse = new CreateChallengeResponse(
                challengeId,
                "Test Challenge",
                "Test Description",
                100,
                ChallengeType.CODING,
                null,
                null,
                null,
                null,
                Instant.now()
        );

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(challengeMapper.fromEntity(challenge)).thenReturn(expectedResponse);

        // Act
        CreateChallengeResponse result = challengeService.getChallengeById(challengeId);

        // Assert
        assertNotNull(result);
        assertEquals(challengeId, result.challengeId());
        assertEquals("Test Challenge", result.title());
        assertEquals("Test Description", result.description());
        assertEquals(100, result.points());
        assertEquals(ChallengeType.CODING, result.challengeType());

        verify(challengeRepository).findById(challengeId);
        verify(challengeMapper).fromEntity(challenge);
    }

    @Test
    void getChallengeById_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long challengeId = 999L;
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            challengeService.getChallengeById(challengeId);
        });

        verify(challengeRepository).findById(challengeId);
        verify(challengeMapper, never()).fromEntity(any());
    }

    @Test
    void getChallengesByHuntId_WithValidHuntId_ShouldReturnChallenges() {
        // Arrange
        Long huntId = 1L;
        Hunt hunt = new Hunt();
        hunt.setId(huntId);

        Challenge challenge1 = new Challenge();
        challenge1.setId(1L);
        challenge1.setTitle("Challenge 1");
        challenge1.setHunt(hunt);

        Challenge challenge2 = new Challenge();
        challenge2.setId(2L);
        challenge2.setTitle("Challenge 2");
        challenge2.setHunt(hunt);

        List<Challenge> challenges = List.of(challenge1, challenge2);

        CreateChallengeResponse response1 = new CreateChallengeResponse(
                1L, "Challenge 1", "Description 1", 100, ChallengeType.CODING,
                null, null, null, null, Instant.now()
        );

        CreateChallengeResponse response2 = new CreateChallengeResponse(
                2L, "Challenge 2", "Description 2", 200, ChallengeType.BUGFIX,
                null, null, null, null, Instant.now()
        );

        when(huntRepository.findById(huntId)).thenReturn(Optional.of(hunt));
        when(challengeRepository.findByHuntId(huntId)).thenReturn(challenges);
        when(challengeMapper.fromEntity(challenge1)).thenReturn(response1);
        when(challengeMapper.fromEntity(challenge2)).thenReturn(response2);

        // Act
        List<CreateChallengeResponse> result = challengeService.getChallengesByHuntId(huntId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).challengeId());
        assertEquals("Challenge 1", result.get(0).title());
        assertEquals(2L, result.get(1).challengeId());
        assertEquals("Challenge 2", result.get(1).title());

        verify(huntRepository).findById(huntId);
        verify(challengeRepository).findByHuntId(huntId);
        verify(challengeMapper, times(2)).fromEntity(any(Challenge.class));
    }

    @Test
    void deleteChallenge_WithValidId_ShouldDeleteChallenge() {
        // Arrange
        Long challengeId = 1L;
        Challenge challenge = new Challenge();
        challenge.setId(challengeId);
        challenge.setTitle("Test Challenge");
        challenge.setMapPieceUri("test-image.jpg");

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        // Mock the static method using try-with-resources and a lambda
        try (var mocked = mockStatic(FIleUploadUtil.class)) {
            mocked.when(() -> FIleUploadUtil.deleteFile(anyString(), anyString())).thenReturn(true);

            // Act
            challengeService.deleteChallenge(challengeId);

            // Assert
            verify(challengeRepository).findById(challengeId);
            verify(challengeRepository).delete(challenge);
            mocked.verify(() -> FIleUploadUtil.deleteFile(anyString(), eq("test-image.jpg")));
        }
    }

    @Test
    void deleteChallenge_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long challengeId = 999L;
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            challengeService.deleteChallenge(challengeId);
        });

        verify(challengeRepository).findById(challengeId);
        verify(challengeRepository, never()).delete(any());
    }
}
