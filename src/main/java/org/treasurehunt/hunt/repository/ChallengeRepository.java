package org.treasurehunt.hunt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.treasurehunt.hunt.repository.entity.Challenge;
import org.treasurehunt.hunt.repository.entity.TestCase;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByHuntId(Long huntId);

    Long countByHunt_Id(Long huntId);

    @Query("""
            SELECT testCase FROM TestCase testCase where testCase.challenge.id=:challengeId
            """)
    List<TestCase> findTestCasesByChallengeId(Long challengeId);

    @Query("SELECT c.mapPieceUri FROM Challenge c WHERE c.id = :id")
    String getImageById(Long id);

}
