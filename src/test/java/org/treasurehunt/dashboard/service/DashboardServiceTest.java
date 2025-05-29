package org.treasurehunt.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.dashboard.api.DashboardResponse;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    private DashboardService dashboardService;

    @Mock
    private HuntRepository huntRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubmissionRepo submissionRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dashboardService = new DashboardService(
                huntRepository,
                challengeRepository,
                userRepository,
                submissionRepo
        );
    }

    @Test
    void getDashboardData_WithHuntAndCompletedChallenges_ShouldReturnCorrectData() {
        // Arrange
        Long userId = 1L;
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "test@example.com", "password", List.of());
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        Hunt hunt = new Hunt();
        hunt.setId(1L);
        hunt.setTitle("Test Hunt");
        Instant endDate = Instant.now().plusSeconds(86400); // 1 day from now
        hunt.setEndDate(endDate);
        
        Challenge challenge1 = new Challenge();
        challenge1.setId(1L);
        challenge1.setTitle("Challenge 1");
        
        Challenge challenge2 = new Challenge();
        challenge2.setId(2L);
        challenge2.setTitle("Challenge 2");
        
        List<Challenge> challenges = List.of(challenge1, challenge2);
        hunt.setChallenges(challenges);
        
        Submission submission1 = new Submission();
        submission1.setStatus(Submission.SubmissionStatus.SUCCESS);
        
        // Mock static method
        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(huntRepository.findHuntByUser_Id(userId)).thenReturn(hunt);
            when(submissionRepo.findByChallengeIdAndUserId(1L, userId))
                    .thenReturn(List.of(submission1));
            when(submissionRepo.findByChallengeIdAndUserId(2L, userId))
                    .thenReturn(new ArrayList<>());
            
            // Act
            DashboardResponse response = dashboardService.getDashboardData();
            
            // Assert
            assertNotNull(response);
            assertEquals(1, response.getData().size());
            
            DashboardResponse.DashboardItem item = response.getData().get(0);
            assertEquals("1", item.getId());
            assertEquals("Test Hunt", item.getTitle());
            assertEquals(50.0, item.getProgress()); // 1 out of 2 challenges completed
            assertEquals(1, item.getCompletedChallenges());
            assertEquals(2, item.getTotalChallenges());
            assertEquals(endDate.atZone(ZoneId.systemDefault()).toLocalDate(), item.getDueDate());
            
            // Verify interactions
            verify(userRepository).findById(userId);
            verify(huntRepository).findHuntByUser_Id(userId);
            verify(submissionRepo).findByChallengeIdAndUserId(1L, userId);
            verify(submissionRepo).findByChallengeIdAndUserId(2L, userId);
        }
    }

    @Test
    void getDashboardData_WithHuntButNoCompletedChallenges_ShouldReturnZeroProgress() {
        // Arrange
        Long userId = 1L;
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "test@example.com", "password", List.of());
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        Hunt hunt = new Hunt();
        hunt.setId(1L);
        hunt.setTitle("Test Hunt");
        hunt.setEndDate(Instant.now().plusSeconds(86400)); // 1 day from now
        
        Challenge challenge1 = new Challenge();
        challenge1.setId(1L);
        challenge1.setTitle("Challenge 1");
        
        Challenge challenge2 = new Challenge();
        challenge2.setId(2L);
        challenge2.setTitle("Challenge 2");
        
        List<Challenge> challenges = List.of(challenge1, challenge2);
        hunt.setChallenges(challenges);
        
        // Mock static method
        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(huntRepository.findHuntByUser_Id(userId)).thenReturn(hunt);
            when(submissionRepo.findByChallengeIdAndUserId(anyLong(), anyLong()))
                    .thenReturn(new ArrayList<>());
            
            // Act
            DashboardResponse response = dashboardService.getDashboardData();
            
            // Assert
            assertNotNull(response);
            assertEquals(1, response.getData().size());
            
            DashboardResponse.DashboardItem item = response.getData().get(0);
            assertEquals("1", item.getId());
            assertEquals("Test Hunt", item.getTitle());
            assertEquals(0.0, item.getProgress()); // 0 out of 2 challenges completed
            assertEquals(0, item.getCompletedChallenges());
            assertEquals(2, item.getTotalChallenges());
            
            // Verify interactions
            verify(userRepository).findById(userId);
            verify(huntRepository).findHuntByUser_Id(userId);
            verify(submissionRepo, times(2)).findByChallengeIdAndUserId(anyLong(), anyLong());
        }
    }

    @Test
    void getDashboardData_WithNoHunt_ShouldReturnEmptyResponse() {
        // Arrange
        Long userId = 1L;
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "test@example.com", "password", List.of());
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        // Mock static method
        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(huntRepository.findHuntByUser_Id(userId)).thenReturn(null);
            
            // Act
            DashboardResponse response = dashboardService.getDashboardData();
            
            // Assert
            assertNotNull(response);
            assertEquals(0, response.getData().size());
            
            // Verify interactions
            verify(userRepository).findById(userId);
            verify(huntRepository).findHuntByUser_Id(userId);
            verify(submissionRepo, never()).findByChallengeIdAndUserId(anyLong(), anyLong());
        }
    }
}