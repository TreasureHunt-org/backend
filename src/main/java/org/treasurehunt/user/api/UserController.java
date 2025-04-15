package org.treasurehunt.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.auth.UserAuthResponse;
import org.treasurehunt.common.api.ApiResp;
import org.treasurehunt.common.api.PageDTO;
import org.treasurehunt.common.api.PageResponse;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.user.service.UserService;
import org.treasurehunt.user.repository.UserSearchCriteria;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.treasurehunt.common.constants.PathConstants.USER_BASE;
import static org.treasurehunt.common.constants.PathConstants.USER_IMAGE;
import static org.treasurehunt.common.constants.UploadingConstants.USER_UPLOAD_DIR;

@Tag(name = "Users Management",
        description = "API Endpoints for managing users")
@RestController
@RequestMapping(USER_BASE)
public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Fetch all users",
            description = "Retrieves a paginated list of users based on the provided search criteria. Only users with the 'ADMIN' authority can access this endpoint.",
            security = {@SecurityRequirement(name = "bearer-key")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing authentication token",
                    content = @Content(
                            schema = @Schema(implementation = ApiResp.class),
                            examples = @ExampleObject(
                                    value = "{ \"success\": false, \"message\": \"Unauthorized\", \"data\": null, \"errors\": [\"Invalid or missing authentication token\"], \"errorCode\": 401, \"timestamp\": 1699999999999 }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User does not have the required permissions",
                    content = @Content(
                            schema = @Schema(implementation = ApiResp.class),
                            examples = @ExampleObject(
                                    value = "{ \"success\": false, \"message\": \"Forbidden\", \"data\": null, \"errors\": [\"User does not have the required permissions\"], \"errorCode\": 403, \"timestamp\": 1699999999999 }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected server issue",
                    content = @Content(
                            schema = @Schema(implementation = ApiResp.class),
                            examples = @ExampleObject(
                                    value = "{ \"success\": false, \"message\": \"Internal Server Error\", \"data\": null, \"errors\": [\"Unexpected server issue\"], \"errorCode\": 500, \"timestamp\": 1699999999999 }"
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<ApiResp<PageResponse<UserAuthResponse>>> getAllUsers(
            PageDTO pageDTO,
            UserSearchCriteria searchCriteria
    ) {
        PageResponse<UserAuthResponse> userPageResponse = userService.searchUsers(pageDTO, searchCriteria);
        return ResponseEntity.ok(
                ApiResp.success(
                        List.of(userPageResponse),
                        "Fetched Users successfully"
                )
        );
    }

    @Operation(
            summary = "Upload user profile image",
            description = "Allows a user to upload their profile picture. Only PNG and JPG files are allowed.",
            security = {@SecurityRequirement(name = "bearer-key")}
    )
    @PostMapping(USER_IMAGE)
    public ResponseEntity<String> uploadUserPicture(
            @Parameter(description = "User ID to associate the image with", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Profile image file (PNG or JPG)", required = true)
            @RequestParam("image") @NotNull MultipartFile image
    ) throws IOException {
        var userDetails = getUserFromSecurityContext()
                .orElseThrow(() -> new EntityNotFoundException("No authenticated user found"));
        if (!userDetails.getId().equals(id) &&
            userDetails.getAuthorities()
                    .stream()
                    .anyMatch(role -> role.getAuthority().equals(Roles.ADMIN.name()))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not allowed");
        }

        userService.updateUserPicture(id, image);
        return ResponseEntity.status(HttpStatus.CREATED).body("Profile image uploaded successfully.");
    }

    @GetMapping(USER_IMAGE)
    public ResponseEntity<Resource> getUserImage(@PathVariable Long id) {
        try {
            String profilePic = userService.getProfilePic(id);
            if (!StringUtils.isNotBlank(profilePic)) {
                return ResponseEntity.ok(null);
            }

            Path filePath = Paths.get(USER_UPLOAD_DIR).resolve(profilePic).normalize();
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private Optional<UserDetailsDTO> getUserFromSecurityContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal() instanceof UserDetailsDTO user ? Optional.of(user) : Optional.empty();
    }
}
