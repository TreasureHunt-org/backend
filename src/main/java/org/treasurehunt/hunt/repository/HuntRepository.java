package org.treasurehunt.hunt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.treasurehunt.hunt.repository.entity.Hunt;

import java.util.List;

@Repository
public interface HuntRepository extends JpaRepository<Hunt, Long>, JpaSpecificationExecutor<Hunt> {
    List<Hunt> findAllByReviewer_Id(Long reviewerId);

    @Query("SELECT h.huntImgUri FROM Hunt h WHERE h.id = :id")
    String getHuntBgImageById(Long id);

    @Query("SELECT h.mapImgUri FROM Hunt h WHERE h.id = :id")
    String getHuntMapImageById(Long id);

    @Query("""
            SELECT count(*) from User u where u.hunt.id=:huntId
            """)
    Long countParticipants(Long huntId);

    @Query("""
            SELECT h from Hunt h where h.id = (SELECT u.hunt.id FROM User u where u.id = :id)
            """)
    Hunt findHuntByUser_Id(Long id);

}
