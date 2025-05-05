package org.treasurehunt.submissions.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.service.SubmissionService;

@RequiredArgsConstructor
@RestController("submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("challenges/{challengeId}/submissions")
    public ResponseEntity<Submission> submitChallengeSolution(
            @RequestBody @Valid ChallengeSubmitRequest request,
            @PathVariable Long challengeId){

        return ResponseEntity.ok(submissionService.submit(challengeId, request));
    }
}
