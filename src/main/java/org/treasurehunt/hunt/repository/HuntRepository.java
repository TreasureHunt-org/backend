package org.treasurehunt.hunt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.treasurehunt.hunt.repository.entity.Hunt;

@Repository
public interface HuntRepository extends JpaRepository<Hunt, Long> {
}
