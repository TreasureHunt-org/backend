package org.treasurehunt.hunt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.CommentRequest;
import org.treasurehunt.hunt.api.CommentResponse;
import org.treasurehunt.hunt.api.DraftHuntDTO;
import org.treasurehunt.hunt.api.LocationDTO;
import org.treasurehunt.hunt.mapper.HuntMapper;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.CommentRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.LocationRepository;
import org.treasurehunt.user.service.UserService;
import org.treasurehunt.common.validation.ValidatorService;
import org.treasurehunt.hunt.repository.entity.Comment;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HuntServiceTest {

    private HuntService huntService;

    @Mock
    private HuntRepository huntRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserService userService;

    @Mock
    private ValidatorService validatorService;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private HuntMapper huntMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private SubmissionRepo submissionRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        huntService = new HuntService(
                huntRepository,
                locationRepository,
                userService,
                validatorService,
                objectMapper,
                huntMapper,
                userRepository,
                commentRepository,
                challengeRepository,
                submissionRepo
        );
    }

    @Test
    void getHunt_WithValidId_ShouldReturnHunt() {
        // Arrange
        Long huntId = 1L;
        Long userId = 1L;

        Hunt hunt = new Hunt();
        hunt.setId(huntId);
        hunt.setTitle("Test Hunt");
        hunt.setDescription("Test Description");
        hunt.setStatus(HuntStatus.DRAFT);

        DraftHuntDTO expectedResponse = new DraftHuntDTO(
                huntId,
                "Test Hunt",
                "Test Description",
                2L,
                3L,
                Instant.now(),
                Instant.now().plusSeconds(86400),
                HuntStatus.DRAFT,
                new LocationDTO(1.0, 2.0)
        );

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Roles.ADMIN.name()));
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "admin@example.com", "password", authorities);

        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));

            when(huntRepository.findById(huntId)).thenReturn(Optional.of(hunt));
            when(huntMapper.toDraftDTO(hunt)).thenReturn(expectedResponse);

            // Act
            DraftHuntDTO result = huntService.getHunt(huntId);

            // Assert
            assertNotNull(result);
            assertEquals(huntId, result.id());
            assertEquals("Test Hunt", result.title());
            assertEquals("Test Description", result.description());
            assertEquals(HuntStatus.DRAFT, result.huntStatus());

            verify(huntRepository).findById(huntId);
            verify(huntMapper).toDraftDTO(hunt);
        }
    }

    @Test
    void getHunt_WithInvalidId_ShouldThrowException() {
        // Arrange
        Long huntId = 999L;
        Long userId = 1L;

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Roles.ADMIN.name()));
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "admin@example.com", "password", authorities);

        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));

            when(huntRepository.findById(huntId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> {
                huntService.getHunt(huntId);
            });

            verify(huntRepository).findById(huntId);
            verify(huntMapper, never()).toDraftDTO(any());
        }
    }

    @Test
    void addComment_WithValidRequest_ShouldAddComment() {
        // Arrange
        Long huntId = 1L;
        Long userId = 1L;
        String commentContent = "This is a test comment";

        Hunt hunt = new Hunt();
        hunt.setId(huntId);
        hunt.setTitle("Test Hunt");

        User user = new User();
        user.setId(userId);
        user.setUsername("admin");

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setHunt(hunt);
        savedComment.setContent(commentContent);
        savedComment.setReviewer(user);

        CommentRequest request = new CommentRequest(commentContent);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Roles.REVIEWER.name()));
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "reviewer@example.com", "password", authorities);

        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));

            when(huntRepository.findById(huntId)).thenReturn(Optional.of(hunt));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

            // Act
            CommentResponse response = huntService.addComment(huntId, request);

            // Assert
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals(huntId, response.getHuntId());
            assertEquals(userId, response.getReviewerId());
            assertEquals(commentContent, response.getContent());

            verify(huntRepository).findById(huntId);
            verify(userRepository).findById(userId);
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Test
    void addComment_WithNonAdminUser_ShouldThrowException() {
        // Arrange
        Long huntId = 1L;
        Long userId = 1L;
        String commentContent = "This is a test comment";

        Hunt hunt = new Hunt();
        hunt.setId(huntId);
        hunt.setTitle("Test Hunt");

        CommentRequest request = new CommentRequest(commentContent);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Roles.HUNTER.name()));
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "hunter@example.com", "password", authorities);

        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));

            when(huntRepository.findById(huntId)).thenReturn(Optional.of(hunt));

            // Act & Assert
            assertThrows(AccessDeniedException.class, () -> {
                huntService.addComment(huntId, request);
            });

            verify(huntRepository).findById(huntId);
            verify(userRepository, never()).findById(anyLong());
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Test
    void joinHunt_WithValidRequest_ShouldJoinHunt() {
        // Arrange
        Long huntId = 1L;
        Long userId = 1L;

        Hunt hunt = new Hunt();
        hunt.setId(huntId);
        hunt.setTitle("Test Hunt");

        User user = new User();
        user.setId(userId);
        user.setUsername("hunter");
        user.setHunt(null); // User is not in a hunt yet

        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "hunter@example.com", "password", List.of());

        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));

            when(huntRepository.findById(huntId)).thenReturn(Optional.of(hunt));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act
            huntService.joinHunt(huntId);

            // Assert
            assertEquals(hunt, user.getHunt());

            verify(huntRepository).findById(huntId);
            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }
    }

    @Test
    void joinHunt_WithUserAlreadyInHunt_ShouldThrowException() {
        // Arrange
        Long huntId = 1L;
        Long userId = 1L;

        Hunt existingHunt = new Hunt();
        existingHunt.setId(2L);
        existingHunt.setTitle("Existing Hunt");

        Hunt newHunt = new Hunt();
        newHunt.setId(huntId);
        newHunt.setTitle("New Hunt");

        User user = new User();
        user.setId(userId);
        user.setUsername("hunter");
        user.setHunt(existingHunt); // User is already in a hunt

        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(userId, "hunter@example.com", "password", List.of());

        try (var authUtilMock = mockStatic(AuthUtil.class)) {
            authUtilMock.when(() -> AuthUtil.getUserFromSecurityContext())
                    .thenReturn(Optional.of(userDetailsDTO));

            when(huntRepository.findById(huntId)).thenReturn(Optional.of(newHunt));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // Act & Assert
            assertThrows(BadRequestException.class, () -> {
                huntService.joinHunt(huntId);
            });

            verify(huntRepository).findById(huntId);
            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any(User.class));
        }
    }
}
