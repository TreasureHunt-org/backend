package org.treasurehunt.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    private final String testSecret = "testSecretKeyForJwtThatIsLongEnoughForHmac256";
    private final Long testExpiration = 3600000L; // 1 hour
    private final Long testRefreshExpiration = 86400000L; // 24 hours
    private final String testUsername = "testuser@example.com";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(userDetailsService, testSecret, testExpiration, testRefreshExpiration);
    }
    
    @Test
    void createJwtAccessToken_ShouldCreateValidToken() {
        // Act
        String token = jwtService.createJwtAccessToken(testUsername);
        
        // Assert
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }
    
    @Test
    void createJWTRefreshToken_ShouldCreateValidToken() {
        // Act
        String refreshToken = jwtService.createJWTRefreshToken(testUsername);
        
        // Assert
        assertNotNull(refreshToken);
        assertTrue(jwtService.validateToken(refreshToken));
    }
    
    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.string";
        
        // Act & Assert
        assertFalse(jwtService.validateToken(invalidToken));
    }
}