package org.treasurehunt.requestedcodes.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestedCodesRepo extends JpaRepository<RequestedCode, Long> {

    @Query("SELECT rc FROM RequestedCode rc WHERE rc.hunt_id = :huntId")
    Optional<RequestedCode> findByHuntId(long huntId);
}
