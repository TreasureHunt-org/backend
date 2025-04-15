package org.treasurehunt.hunt.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.treasurehunt.common.api.ApiResp;
import org.treasurehunt.exception.AuthenticationFailedException;
import org.treasurehunt.exception.BadRequestException;
import org.treasurehunt.hunt.mapper.HuntMapper;
import org.treasurehunt.hunt.service.ChallengeService;
import org.treasurehunt.hunt.service.HuntService;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.security.UserDetailsDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.treasurehunt.common.constants.PathConstants.HUNT_BASE;
import static org.treasurehunt.common.constants.PathConstants.HUNT_ID_CHALLENGE;


@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping(HUNT_BASE)
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

    @PreAuthorize("hasAuthority('ORGANIZER')" )
    @PostMapping
    public ResponseEntity<DraftHuntDTO> createHunt(
            @RequestPart("huntData" ) String huntData,
            @RequestPart("background" ) MultipartFile bgImage,
            @RequestPart("map" ) MultipartFile mapImage
    ) throws IOException {
        UserDetailsDTO userDetailsDTO = getUserFromSecurityContext().orElseThrow(
                () -> new EntityNotFoundException("No authenticated user found" )
        );

        if (bgImage == null || bgImage.isEmpty() || mapImage == null || mapImage.isEmpty()) {
            throw new BadRequestException("Both background and map images are required." );
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
    @PutMapping(HUNT_ID_CHALLENGE)
    public ResponseEntity<String> addChallenge(
            @PathVariable Long id,
            @Valid @RequestBody CreateChallengeDTO createChallengeDTO
    ) {

        System.out.println(id);
        UserDetailsDTO user = getUserFromSecurityContext()
                .orElseThrow(() -> new AuthenticationFailedException("Authentication failed" ));

        challengeService.createChallenge(id, user.getId(), createChallengeDTO);

        return ResponseEntity.ok("Challenge added" );
    }

    @GetMapping
    public ResponseEntity<ApiResp<DraftHuntDTO>> getAllDraftedHunts() {
        List<DraftHuntDTO> draftedHunts = huntMapper.toDraftDTOs(huntService.getAllDrafted());
        return ResponseEntity.ok(
                ApiResp.success(
                        draftedHunts, "Retrieved all drafted hunts"
                )
        );
    }

    private Optional<UserDetailsDTO> getUserFromSecurityContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal() instanceof UserDetailsDTO user ? Optional.of(user) : Optional.empty();
    }

}
