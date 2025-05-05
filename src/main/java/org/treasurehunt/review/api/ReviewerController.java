package org.treasurehunt.review.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.treasurehunt.hunt.api.DraftHuntDTO;
import org.treasurehunt.review.service.ReviewerService;
import org.treasurehunt.security.UserDetailsDTO;

import java.util.List;
import java.util.Optional;

import static org.treasurehunt.common.util.AuthUtil.getUserFromSecurityContext;


@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping()
@Tag(name = "Reviewer Management", description = "APIs for managing reviews on hunts")
public class ReviewerController {

    private final ReviewerService reviewerService;

    // TODO handle submit for review

    @GetMapping("/hunts/reviewers/me")
    public ResponseEntity<List<DraftHuntDTO>> getReviewerHunts(
    ){
        return ResponseEntity.ok(
                reviewerService.getReviewerHunts()
        );
    }

    @PutMapping("/hunts/{huntId}")
    public ResponseEntity<Void> submitHuntForReview(
            @PathVariable Long huntId
    ){
        UserDetailsDTO userFromSecurityContext = getUserFromSecurityContext().orElseThrow(
                () -> new EntityNotFoundException("No authenticated user found")
        );

        reviewerService.submitHuntToReview(huntId, userFromSecurityContext.getId());

        return ResponseEntity.ok(null);
    }

    @PostMapping("/reviewers/hunts/{huntId}")
    public ResponseEntity<Void> assignReviewerToHunt(
            @PathVariable Long huntId
    ){
        reviewerService.assignReviewer(huntId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reviewers/hunts/{huntId}/approve")
    public ResponseEntity<Void> approveReviewerToHunt(
            @PathVariable Long huntId
    ){
        reviewerService.approveHunt(huntId);
        return ResponseEntity.ok(null);
    }
}
