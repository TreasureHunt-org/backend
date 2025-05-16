package org.treasurehunt.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.treasurehunt.common.util.AuthUtil;
import org.treasurehunt.dashboard.api.DashboardResponse;
import org.treasurehunt.exception.EntityNotFoundException;
import org.treasurehunt.hunt.repository.ChallengeRepository;
import org.treasurehunt.hunt.repository.HuntRepository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.Hunt;
import org.treasurehunt.submissions.repo.Submission;
import org.treasurehunt.submissions.repo.SubmissionRepo;
import org.treasurehunt.user.repository.UserRepository;
import org.treasurehunt.user.repository.entity.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DashboardService {

    private final HuntRepository huntRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final SubmissionRepo submissionRepo;

    public DashboardResponse getDashboardData() {
        // Get the current user
        Long userId = AuthUtil.getUserFromSecurityContext()
                .orElseThrow(() -> new EntityNotFoundException("No user found"))
                .getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(userId, User.class));

        // Get the hunt associated with the user
        Hunt hunt = huntRepository.findHuntByUser_Id(userId);
        if (hunt == null) {
            // Return empty response if user has no hunt
            return new DashboardResponse(new ArrayList<>());
        }

        // Get all challenges for the hunt
        List<Challenge> challenges = hunt.getChallenges();
        int totalChallenges = challenges.size();

        // Count completed challenges
        int completedChallenges = 0;
        for (Challenge challenge : challenges) {
            List<Submission> submissions = submissionRepo.findByChallengeIdAndUserId(challenge.getId(), userId);
            boolean solved = submissions.stream()
                    .anyMatch(s -> s.getStatus().equals(Submission.SubmissionStatus.SUCCESS));
            if (solved) {
                completedChallenges++;
            }
        }

        // Calculate progress
        double progress = totalChallenges > 0 ? (double) completedChallenges / totalChallenges * 100 : 0;

        // Convert endDate to LocalDate
        LocalDate dueDate = hunt.getEndDate() != null 
            ? hunt.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate() 
            : null;

        // Create dashboard item
        DashboardResponse.DashboardItem item = DashboardResponse.DashboardItem.builder()
                .id(hunt.getId().toString())
                .title(hunt.getTitle())
                .progress(progress)
                .completedChallenges(completedChallenges)
                .totalChallenges(totalChallenges)
                .dueDate(dueDate)
                .build();

        // Create and return response
        return new DashboardResponse(List.of(item));
    }
}