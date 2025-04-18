package org.treasurehunt.hunt.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.common.api.ApiResp;
import org.treasurehunt.common.enums.HuntStatus;
import org.treasurehunt.exception.AuthenticationFailedException;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.hunt.mapper.HuntMapper;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.service.ChallengeService;
import org.treasurehunt.hunt.service.HuntService;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.security.UserDetailsDTO;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.treasurehunt.common.constants.PathConstants.*;


@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping(HUNT_BASE)
@Tag(name = "Hunt Management", description = "APIs for managing treasure hunts")
public class HuntController {

    /*
    TODO
        -Allow Users with Organizer Role to create a hunt
            -When a hunt is fully created it will be submitted for review [ POST /hunts/{id}/submit ]
            -Drafted Hunt
                -[ POST /hunts/drafts ] Create a hunt in DRAFT state (Save Draft)
                -[ PUT /hunts/drafts/{id} ]	Update an existing draft hunt
                -
        -Retrieve Hunts using Pagination && Filtering
     */
    private final HuntService huntService;
    private final HuntMapper huntMapper;
    private final ChallengeService challengeService;
    private final ChallengeRepository challengeRepository;

    @Operation(
            summary = "Create a new hunt",
            description = "Creates a new hunt in DRAFT state with the provided data and images. Requires ORGANIZER role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Hunt created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DraftHuntDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing required images",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResp.ErrorExample.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResp.ErrorExample.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ORGANIZER role",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResp.ErrorExample.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<DraftHuntDTO> createHunt(
            @Parameter(description = "Hunt data in JSON format") @RequestPart("huntData") String huntData,
            @Parameter(description = "Background image file") @RequestPart("background") MultipartFile bgImage,
            @Parameter(description = "Map image file") @RequestPart("map") MultipartFile mapImage
    ) throws IOException {
        UserDetailsDTO userDetailsDTO = getUserFromSecurityContext().orElseThrow(
                () -> new EntityNotFoundException("No authenticated user found")
        );

        if (bgImage == null || bgImage.isEmpty() || mapImage == null || mapImage.isEmpty()) {
            throw new BadRequestException("Both background and map images are required.");
        }

        Hunt draftedHunt = huntService.draftHunt(huntData, bgImage, mapImage, userDetailsDTO.getId());
        DraftHuntDTO draftHuntDTO = huntMapper.toDraftDTO(draftedHunt);
        return ResponseEntity.status(CREATED).body(
                draftHuntDTO
        );
    }

    /***
     * TODO
     *  Accept Challenge Data to create it in a requestBody, like:
     *      - Challenge type
     *      - Challenge Code
     *      - Challenge Question
     *      - Meta data maybe ? about the coding or the bugfix challenges
     *      - If it is a game then they need to provide a URL for the hosted game [Requires to create other endpoints later on]
     *      - Test cases in case it was a bugfix or a coding challenge
     *      - Points on a challenge
     *      - etc.. as we go on we discover more things to add
     */
    @Operation(
            summary = "Add a challenge to a hunt",
            description = "Adds a new challenge to an existing hunt with an optional map piece image"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or unsupported image format"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "404", description = "Hunt not found")
    })
    @PutMapping(HUNT_ID_CHALLENGE)
    public ResponseEntity<CreateChallengeResponse> addChallenge(
            @Parameter(description = "Hunt ID") @PathVariable Long id,
            @Parameter(description = "Challenge data") @Valid @RequestPart("challengeData") String createChallengeDTO,
            @Parameter(description = "Challenge map piece image") @RequestPart(value = "image") MultipartFile image
    ) throws IOException {
        UserDetailsDTO user = getUserFromSecurityContext()
                .orElseThrow(() -> new AuthenticationFailedException("Authentication failed"));

        CreateChallengeResponse challenge = challengeService.createChallenge(id, user.getId(), createChallengeDTO, image);

        return ResponseEntity.ok(challenge);
    }

    @Operation(
            summary = "Get all hunts",
            description = "Retrieves a paginated list of hunts with optional filtering"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of hunts"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResp.ErrorExample.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResp.ErrorExample.class)))
    })
    @GetMapping
    public ResponseEntity<Page<DraftHuntDTO>> getAllHunts(
            @Parameter(description = "Page number (zero-based)") 
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size") 
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field") 
            @RequestParam(defaultValue = "id") String sort,

            @Parameter(description = "Sort direction (ASC or DESC)") 
            @RequestParam(defaultValue = "ASC") String direction,

            @Parameter(description = "Filter by title (case-insensitive, partial match)") 
            @RequestParam(required = false) String title,

            @Parameter(description = "Filter by hunt status") 
            @RequestParam(required = false) HuntStatus status,

            @Parameter(description = "Filter by organizer ID") 
            @RequestParam(required = false) Long organizerId,

            @Parameter(description = "Filter by start date (from)") 
            @RequestParam(required = false) Instant startDateFrom,

            @Parameter(description = "Filter by start date (to)") 
            @RequestParam(required = false) Instant startDateTo,

            @Parameter(description = "Filter by end date (from)") 
            @RequestParam(required = false) Instant endDateFrom,

            @Parameter(description = "Filter by end date (to)") 
            @RequestParam(required = false) Instant endDateTo
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.fromString(direction), sort);

        HuntFilter filter = HuntFilter.builder()
                .title(title)
                .status(status)
                .organizerId(organizerId)
                .startDateFrom(startDateFrom)
                .startDateTo(startDateTo)
                .endDateFrom(endDateFrom)
                .endDateTo(endDateTo)
                .build();

        Page<Hunt> hunts = huntService.getAllHunts(pageable, filter);
        Page<DraftHuntDTO> huntDTOs = hunts.map(huntMapper::toDraftDTO);

        return ResponseEntity.ok(huntDTOs);
    }


    @GetMapping(CHALLENGE_BASE)
    public ResponseEntity<List<String>> getAllChallenges() {
        return ResponseEntity.ok(challengeRepository.findAll()
                .stream().map(challenge -> {
                    return challenge.getChallengeCodes().getFirst().getCode();
                }).toList());
    }

    @Operation(
            summary = "Get challenge by ID",
            description = "Retrieves a challenge by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge found and returned successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateChallengeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Challenge not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResp.ErrorExample.class)))
    })
    @GetMapping(CHALLENGE_BASE + "/{id}")
    public ResponseEntity<CreateChallengeResponse> getChallengeById(
            @Parameter(description = "Challenge ID") @PathVariable Long id) {
        CreateChallengeResponse challenge = challengeService.getChallengeById(id);
        return ResponseEntity.status(OK).body(challenge);
    }

    @Operation(
            summary = "Delete challenge by ID",
            description = "Deletes a challenge by its unique identifier and removes its associated image file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Challenge successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication failed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ORGANIZER role"),
            @ApiResponse(responseCode = "404", description = "Challenge not found")
    })
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'ADMIN')")
    @DeleteMapping(CHALLENGE_BASE + "/{id}")
    public ResponseEntity<ApiResp<Void>> deleteChallenge(
            @Parameter(description = "Challenge ID") @PathVariable Long id) {
        UserDetailsDTO user = getUserFromSecurityContext()
                .orElseThrow(() -> new AuthenticationFailedException("Authentication failed"));

        challengeService.deleteChallenge(id);

        return ResponseEntity.ok(ApiResp.success(null, "Challenge successfully deleted"));
    }

    private Optional<UserDetailsDTO> getUserFromSecurityContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal() instanceof UserDetailsDTO user ? Optional.of(user) : Optional.empty();
    }

}
