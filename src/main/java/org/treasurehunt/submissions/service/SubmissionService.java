package org.treasurehunt.submissions.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.SubmitSolutionResponse;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.TestCase;
import org.treasurehunt.hunt.service.Judge0Service;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.submissions.api.ChallengeSubmitRequest;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubmissionService {

    private final Judge0Service judge0Service;
    private final ChallengeRepository challengeRepository;
    private final SubmissionRepo submissionRepo;


    public Submission submit(Long challengeId, ChallengeSubmitRequest request) {
        UserDetailsDTO user = AuthUtil.getUserFromSecurityContext().orElseThrow();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("No such challenge exists"));

        List<TestCase> testCases = challengeRepository.findTestCasesByChallengeId(challenge.getId());

        var testCaseResults = judge0Service.validateCode(request.code(), request.language(), testCases);

        Submission submission = new Submission();
        submission.setUserId(user.getId());
        submission.setChallengeId(challengeId);
        submission.setCode(request.code());
        submission.setLanguage(request.language().name());
        submission.setTime(Instant.now());

        boolean allPassed = testCaseResults.stream()
                .allMatch(SubmitSolutionResponse.TestCaseResult::passed);

        submission.setStatus(allPassed ? Submission.SubmissionStatus.SUCCESS : Submission.SubmissionStatus.FAIL);

        return submissionRepo.save(submission);
    }
}
