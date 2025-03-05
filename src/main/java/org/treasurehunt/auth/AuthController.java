package org.treasurehunt.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.treasurehunt.common.api.ApiResponse;
import org.treasurehunt.exception.AuthenticationFailedException;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.security.jwt.JwtService;
import org.treasurehunt.user.User;
import org.treasurehunt.user.UserService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.treasurehunt.common.constants.AuthConstants.BEARER_PREFIX;
import static org.treasurehunt.common.constants.PathConstants.*;

/**
 * Authentication controller used to handle users authentication.
 *
 * @author Rashed Al Maaitah
 * @version 1.0
 */
@Tag(name = "Authentication",
        description = "Public APIs for managing users authentication and registration")
@RestController
@RequestMapping(AUTH_BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Operation(description = """
            Authenticate a user before accessing any protected resource
            returns a valid access token if it succeeded""")
    @PostMapping(AUTH_SIGNIN)
    public ResponseEntity<ApiResponse<UserAuthResponse>> authenticateUser(@RequestBody @Valid AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );

            String accessToken = jwtService.createJwtAccessToken(authentication.getName());
            String refreshToken = jwtService.createJWTRefreshToken(authentication.getName());

            User user = userService.updateRefreshToken(authentication.getName(), refreshToken);

            var userResponse = new UserAuthResponse(
                    user.getId().toString(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRoles().stream()
                            .map(role -> role.getId().getRoleName())
                            .toArray(String[]::new),
                    refreshToken
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
                    .body(ApiResponse
                            .success(List.of(userResponse),
                                    "Authentication successful"));
        } catch (BadCredentialsException ex) {
            throw new AuthenticationFailedException("Invalid username or password");
        }
    }

    @Operation(
            summary = "Register a new user",
            description = """
                    This endpoint allows for the registration of a new user in the system.
                    The user will need to provide the required details such as first name, last name,
                    email, and password. Upon successful registration, the system will create a new user
                    and return the details of the created user, including a generated ID.
                    """
    )
    @PostMapping(AUTH_SIGNUP)
    public ResponseEntity<ApiResponse<UserAuthResponse>> registerUser(@RequestBody @Valid CreateUserRequest request) {
        UserAuthResponse userAuthResponse = userService.createUser(request);

        return ResponseEntity.status(CREATED)
                .body(ApiResponse.
                        success(List.of(userAuthResponse),
                                "User registered successfully"));
    }

    @Operation(description = """
            An API call, to refresh user JWT token without signing again,
            to be able to continue be authenticated and use the system.
            """)
    @PostMapping(AUTH_REFRESH_TOKEN)
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();

        User user = userService.validateRefreshToken(requestRefreshToken);

        String newAccessToken = jwtService.createJwtAccessToken(user.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + newAccessToken)
                .body(ApiResponse.success(
                        List.of(new RefreshTokenResponse(requestRefreshToken)),
                        "Refresh token successfully validated."
                ));
    }

    @Operation(description = """
            An API call, to logout the user,
            But has to re-authenticate again to access the system.
            """,
            security = {@SecurityRequirement(name = "bearer-key")})
    @PostMapping(AUTH_SIGNOUT)
    public ResponseEntity<ApiResponse<LogoutResponse>> logoutUser() {
        return getUserFromSecurityContext()
                .map(user -> {
                    userService.invalidateRefreshTokenByEmail(user.getUsername());
                    SecurityContextHolder.getContext().setAuthentication(null);

                    ApiResponse<LogoutResponse> apiResponse = ApiResponse.success(
                            List.of(),
                            "You have been successfully logged out."
                    );
                    return ResponseEntity.ok(apiResponse);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated. Please sign in."));
    }

    @Operation(description = """
            An API call, to change the user password,
            the user has to signin again to gain a valid access token back.
            """,
            security = {@SecurityRequirement(name = "bearer-key")})
    @PostMapping(AUTH_CHANGE_PASSWORD)
    public ResponseEntity<ApiResponse<String>> changePassword(
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        Optional<UserDetailsDTO> userDetails = getUserFromSecurityContext();
        if (userDetails.isEmpty())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated. Please sign in.");

        userService.updatePassword(userDetails.get().getId(), changePasswordRequest);
        SecurityContextHolder.getContext().setAuthentication(null);

        return ResponseEntity.ok(
                ApiResponse.success(
                        List.of(),
                        "Password Changed Successfully, Login again"
                ));
    }

    private Optional<UserDetailsDTO> getUserFromSecurityContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal() instanceof UserDetailsDTO user ? Optional.of(user) : Optional.empty();
    }
}