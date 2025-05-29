package org.treasurehunt.submissions.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.service.Judge0Service;
import org.treasurehunt.submissions.api.SubmissionListResponse;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SubmissionServiceTest {

    private SubmissionService submissionService;

    @Mock
    private Judge0Service judge0Service;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private SubmissionRepo submissionRepo;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        submissionService = new SubmissionService(judge0Service, challengeRepository, submissionRepo, userRepository);
    }

    @Test
    void getSubmissionsByHuntIdAndHunterName_WithHuntIdAndHunterName_ShouldReturnFilteredSubmissions() {
        // Arrange
        Long huntId = 1L;
        String hunterName = "testUser";
        
        User user = new User();
        user.setId(1L);
        user.setUsername(hunterName);
        
        Hunt hunt = new Hunt();
        hunt.setId(huntId);
        hunt.setTitle("Test Hunt");
        
        Challenge challenge = new Challenge();
        challenge.setId(1L);
        challenge.setTitle("Test Challenge");
        challenge.setPoints(10);
        challenge.setHunt(hunt);
        
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setUserId(user.getId());
        submission.setChallengeId(challenge.getId());
        submission.setCode("test code");
        submission.setStatus(Submission.SubmissionStatus.SUCCESS);
        submission.setTime(Instant.now());
        
        Page<Submission> submissionPage = new PageImpl<>(Collections.singletonList(submission));
        
        when(submissionRepo.findByHuntIdAndHunterName(eq(huntId), eq(hunterName), any(Pageable.class)))
            .thenReturn(submissionPage);
        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        
        // Act
        SubmissionListResponse response = submissionService.getSubmissionsByHuntIdAndHunterName(huntId, hunterName, 0, 10);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getData().size());
        assertEquals(hunterName, response.getData().get(0).getHunterName());
        assertEquals(challenge.getTitle(), response.getData().get(0).getChallengeTitle());
        assertEquals(hunt.getTitle(), response.getData().get(0).getHuntName());
    }

    @Test
    void getSubmissionsByHuntIdAndHunterName_WithOnlyHuntId_ShouldReturnHuntSubmissions() {
        // Arrange
        Long huntId = 1L;
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        
        Hunt hunt = new Hunt();
        hunt.setId(huntId);
        hunt.setTitle("Test Hunt");
        
        Challenge challenge = new Challenge();
        challenge.setId(1L);
        challenge.setTitle("Test Challenge");
        challenge.setPoints(10);
        challenge.setHunt(hunt);
        
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setUserId(user.getId());
        submission.setChallengeId(challenge.getId());
        submission.setCode("test code");
        submission.setStatus(Submission.SubmissionStatus.SUCCESS);
        submission.setTime(Instant.now());
        
        Page<Submission> submissionPage = new PageImpl<>(Collections.singletonList(submission));
        
        when(submissionRepo.findByHuntId(eq(huntId), any(Pageable.class)))
            .thenReturn(submissionPage);
        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        
        // Act
        SubmissionListResponse response = submissionService.getSubmissionsByHuntIdAndHunterName(huntId, null, 0, 10);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals(hunt.getTitle(), response.getData().get(0).getHuntName());
    }

    @Test
    void getSubmissionsByHuntIdAndHunterName_WithNoParameters_ShouldReturnAllSubmissions() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        
        Hunt hunt = new Hunt();
        hunt.setId(1L);
        hunt.setTitle("Test Hunt");
        
        Challenge challenge = new Challenge();
        challenge.setId(1L);
        challenge.setTitle("Test Challenge");
        challenge.setPoints(10);
        challenge.setHunt(hunt);
        
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setUserId(user.getId());
        submission.setChallengeId(challenge.getId());
        submission.setCode("test code");
        submission.setStatus(Submission.SubmissionStatus.SUCCESS);
        submission.setTime(Instant.now());
        
        Page<Submission> submissionPage = new PageImpl<>(Collections.singletonList(submission));
        
        when(submissionRepo.findAllWithDetails(any(Pageable.class)))
            .thenReturn(submissionPage);
        when(challengeRepository.findById(challenge.getId())).thenReturn(Optional.of(challenge));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        
        // Act
        SubmissionListResponse response = submissionService.getSubmissionsByHuntIdAndHunterName(null, null, 0, 10);
        
        // Assert
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
    }
}