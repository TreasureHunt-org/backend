package org.treasurehunt.submissions.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SubmissionRepo extends JpaRepository<Submission, Long> {

    @Query("""
            SELECT s FROM Submission s where s.challengeId=:challengeId AND s.userId=:userId
            """)
    List<Submission> findByChallengeIdAndUserId(Long challengeId, Long userId);

    @Query("""
            SELECT s FROM Submission s WHERE s.userId=:userId AND s.status='SUCCESS' AND s.time >= :since
            """)
    List<Submission> findSuccessfulSubmissionsByUserIdSince(Long userId, Instant since);
}
