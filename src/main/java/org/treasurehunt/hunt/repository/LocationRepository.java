package org.treasurehunt.hunt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.treasurehunt.hunt.repository.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
}
