package org.treasurehunt.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.security.exception.TokenAuthenticationException;

import java.util.Date;

/**
 * Service for generating and resolving JWT (JSON Web Token) for user authentication and authorization.
 * This class handles the creation of a JWT token, verifying it, and extracting user details from the token.
 * It also manages the signing algorithm and handles various exceptions related to token validity and integrity.
 * <p>
 * The service uses the HMAC256 algorithm for signing the token, with the secret key loaded from the application properties.
 * </p>
 *
 * @author Rashed Al Maaitah
 * @version 1.0
 */
@Service
public class JwtService {

    private final UserDetailsService userDetailsService;
    private final Algorithm signingAlgorithm;
    private final Long jwtExpiration;
    private final Long refreshTokenExpiration;

    public JwtService(UserDetailsService userDetailsService,
                      @Value("${app.security.jwt.secret}") String signingSecret,
                      @Value("${app.security.jwt.expiration}") Long jwtExpiration,
                      @Value("${app.security.jwt.refresh-expiration}") Long refreshTokenExpiration) {
        this.userDetailsService = userDetailsService;
        this.signingAlgorithm = Algorithm.HMAC256(signingSecret);
        this.jwtExpiration = jwtExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Resolves a JWT token and extracts user information (user ID and roles) from it.
     * The token is verified using the signing algorithm, and if the token is valid, an AuthUser object is returned.
     * In case of invalid token, various exceptions are thrown with appropriate error messages.
     *
     * @param token the JWT token to be resolved
     * @return an AuthUser object containing the user ID and roles from the token
     * @throws TokenAuthenticationException if the token is invalid, expired, or has other issues
     */
    public UserDetailsDTO resolveJwtToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(signingAlgorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            String username = decodedJWT.getSubject();

            // load the user from database and place into a UserDetails object
            return (UserDetailsDTO) userDetailsService.loadUserByUsername(username);

        } catch (TokenExpiredException ex) {
            throw new TokenAuthenticationException("Token has expired. Please log in again.", ex);
        } catch (SignatureVerificationException ex) {
            throw new TokenAuthenticationException("Invalid token signature.", ex);
        } catch (AlgorithmMismatchException ex) {
            throw new TokenAuthenticationException("Token was signed with an unexpected algorithm.", ex);
        } catch (MissingClaimException ex) {
            throw new TokenAuthenticationException("Invalid or missing claims in the token.", ex);
        } catch (JWTDecodeException ex) {
            throw new TokenAuthenticationException("Malformed JWT token.", ex);
        } catch (JWTVerificationException ex) {
            throw new TokenAuthenticationException("Token verification failed.", ex);
        }
    }

    /**
     * Creates a new JWT token for the given user. The token includes the user ID, roles, issued time, and expiration time.
     * The token is signed using the configured signing algorithm.
     * <p>
     * The token will be valid for 1 hour after creation.
     * </p>
     *
     * @param username String representing the identity for the user requesting access token
     * @return the generated JWT token as a String
     */
    public String createJwtAccessToken(String username) {
        return generateToken(username, jwtExpiration);
    }

    public String createJWTRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration);
    }

    private String generateToken(String username, Long expirationTime) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + expirationTime;
        Date exp = new Date(expMillis);

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(signingAlgorithm);
    }

    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(signingAlgorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }
}