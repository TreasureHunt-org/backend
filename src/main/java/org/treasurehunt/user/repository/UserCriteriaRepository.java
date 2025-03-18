package org.treasurehunt.user.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.treasurehunt.common.repository.criteria.CriteriaRepository;
import org.treasurehunt.user.repository.entity.User;

@Repository
public class UserCriteriaRepository extends CriteriaRepository<User, UserSearchCriteria> {
    public UserCriteriaRepository(EntityManager entityManager) {
        super(entityManager, new UserCriteriaFilterImpl(), User.class);
    }
}
