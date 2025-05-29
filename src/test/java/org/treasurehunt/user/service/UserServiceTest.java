package org.treasurehunt.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.treasurehunt.auth.ChangePasswordRequest;
import org.treasurehunt.auth.CreateUserRequest;
import org.treasurehunt.auth.UserAuthResponse;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.exception.IncorrectPasswordException;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.security.jwt.JwtService;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.mapper.UserMapper;
import org.treasurehunt.user.repository.UserCriteriaRepository;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserCriteriaRepository userCriteriaRepository;

    @Mock
    private SubmissionRepo submissionRepo;

    @Mock
    private ChallengeRepository challengeRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, userMapper, jwtService, passwordEncoder, 
                                     userCriteriaRepository, submissionRepo, challengeRepository);
    }

    @Test
    void createUser_WithValidRequest_ShouldCreateUserWithUserRole() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
            "testuser",
            "test@example.com",
            "password123"
        );

        User requestUser = new User();
        requestUser.setUsername(request.username());
        requestUser.setEmail(request.email());
        requestUser.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(request.username());
        savedUser.setEmail(request.email());

        UserAuthResponse expectedResponse = new UserAuthResponse(
            savedUser.getId().toString(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            new String[]{Roles.HUNTER.name()},
            null,
            null,
            null
        );

        when(userRepository.findByEmailOrUsernameIgnoreCase(anyString(), anyString())).thenReturn(Optional.empty());
        when(userMapper.toUser(request)).thenReturn(requestUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserAuthResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        UserAuthResponse response = userService.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(savedUser.getId().toString(), response.id());
        assertEquals(savedUser.getUsername(), response.username());
        assertEquals(savedUser.getEmail(), response.email());
        assertArrayEquals(new String[]{Roles.HUNTER.name()}, response.roles());

        verify(userRepository, times(2)).save(any(User.class));
        verify(userMapper).toUser(request);
        verify(userMapper).toUserAuthResponse(savedUser);
    }

    @Test
    void updatePassword_WithValidRequest_ShouldUpdatePassword() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "currentPassword";
        String newPassword = "newPassword";

        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedCurrentPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        // Act
        userService.updatePassword(userId, request);

        // Assert
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(eq(oldPassword), eq("encodedCurrentPassword"));
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
    }

    @Test
    void updatePassword_WithInvalidCurrentPassword_ShouldThrowException() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";

        ChangePasswordRequest request = new ChangePasswordRequest(oldPassword, newPassword);

        User user = new User();
        user.setId(userId);
        user.setPassword("encodedCurrentPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(IncorrectPasswordException.class, () -> {
            userService.updatePassword(userId, request);
        });

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, user.getPassword());
        verify(userRepository, never()).save(any(User.class));
    }
}
