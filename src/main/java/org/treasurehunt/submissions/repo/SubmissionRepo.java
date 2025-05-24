package org.treasurehunt.submissions.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
            SELECT s FROM Submission s 
            JOIN User u ON s.userId = u.id 
            JOIN Challenge c ON s.challengeId = c.id 
            JOIN Hunt h ON c.hunt.id = h.id 
            WHERE h.id = :huntId AND LOWER(u.username) LIKE LOWER(CONCAT('%', :hunterName, '%'))
            ORDER BY s.time DESC
            """)
    Page<Submission> findByHuntIdAndHunterName(Long huntId, String hunterName, Pageable pageable);

    @Query("""
            SELECT s FROM Submission s 
            JOIN User u ON s.userId = u.id 
            JOIN Challenge c ON s.challengeId = c.id 
            JOIN Hunt h ON c.hunt.id = h.id 
            WHERE h.id = :huntId
            ORDER BY s.time DESC
            """)
    Page<Submission> findByHuntId(Long huntId, Pageable pageable);

    @Query("""
                SELECT s FROM Submission s 
                JOIN User u ON s.userId = u.id 
                JOIN Challenge c ON s.challengeId = c.id 
                JOIN Hunt h ON c.hunt.id = h.id 
                WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :hunterName, '%'))
                ORDER BY s.time DESC
            """)
    Page<Submission> findByHunterName(@Param("hunterName") String hunterName, Pageable pageable);


    @Query("""
            SELECT s FROM Submission s 
            JOIN User u ON s.userId = u.id 
            JOIN Challenge c ON s.challengeId = c.id 
            JOIN Hunt h ON c.hunt.id = h.id
            ORDER BY s.time DESC
            """)
    Page<Submission> findAllWithDetails(Pageable pageable);
}
