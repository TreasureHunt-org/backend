package org.treasurehunt.submissions.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.api.SubmitSolutionResponse;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.hunt.repository.entity.TestCase;
import org.treasurehunt.hunt.service.Judge0Service;
import org.treasurehunt.security.UserDetailsDTO;
import org.treasurehunt.submissions.api.ChallengeSubmitRequest;
import org.treasurehunt.submissions.api.SubmissionListResponse;
import org.treasurehunt.submissions.api.SubmissionResponseDTO;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SubmissionService {

    private final Judge0Service judge0Service;
    private final ChallengeRepository challengeRepository;
    private final SubmissionRepo submissionRepo;
    private final UserRepository userRepository;

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

    public SubmissionListResponse getSubmissionsByHuntIdAndHunterName(Long huntId, String hunterName, Integer page, Integer pageSize) {
        // Default values if not provided
        int pageNumber = page != null ? page : 0;
        int size = pageSize != null ? pageSize : 10;

        // Create pageable with sorting by time in descending order
        Pageable pageable = PageRequest.of(pageNumber, size);

        Page<Submission> submissionsPage;

        // Determine which repository method to call based on the provided parameters
        if (huntId != null && hunterName != null) {
            submissionsPage = submissionRepo.findByHuntIdAndHunterName(huntId, hunterName, pageable);
        } else if (huntId != null) {
            submissionsPage = submissionRepo.findByHuntId(huntId, pageable);
        } else if (hunterName != null) {
            submissionsPage = submissionRepo.findByHunterName(hunterName, pageable);
        } else {
            submissionsPage = submissionRepo.findAllWithDetails(pageable);
        }

        List<SubmissionResponseDTO> submissionDTOs = submissionsPage.getContent().stream()
                .map(submission -> {
                    Challenge challenge = challengeRepository.findById(submission.getChallengeId())
                            .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));

                    Hunt hunt = challenge.getHunt();
                    User user = userRepository.findById(submission.getUserId())
                            .orElseThrow(() -> new EntityNotFoundException("User not found"));

                    return SubmissionResponseDTO.builder()
                            .id(submission.getId().toString())
                            .hunterName(user.getUsername())
                            .submissionDate(submission.getTime())
                            .challengeNumber(challenge.getId().intValue())
                            .challengeTitle(challenge.getTitle())
                            .score(challenge.getPoints())
                            .status(submission.getStatus().name())
                            .code(submission.getCode())
                            .huntId(hunt.getId().toString())
                            .huntName(hunt.getTitle())
                            .build();
                })
                .collect(Collectors.toList());

        return SubmissionListResponse.builder()
                .success(true)
                .totalPages(submissionsPage.getTotalPages())
                .data(submissionDTOs)
                .build();
    }
}
