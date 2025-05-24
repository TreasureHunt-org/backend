package org.treasurehunt.submissions.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.service.SubmissionService;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("challenges/{challengeId}/submissions")
    public ResponseEntity<Submission> submitChallengeSolution(
            @RequestBody @Valid ChallengeSubmitRequest request,
            @PathVariable Long challengeId){

        return ResponseEntity.ok(submissionService.submit(challengeId, request));
    }

    @GetMapping("submissions")
    public ResponseEntity<SubmissionListResponse> getSubmissions(
            @RequestParam(value = "huntId", required = false) Long huntId,
            @RequestParam(value = "hunterName", required = false) String hunterName,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        SubmissionListResponse response = submissionService.getSubmissionsByHuntIdAndHunterName(huntId, hunterName, page, pageSize);
        return ResponseEntity.ok(response);
    }
}
