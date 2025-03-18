package org.treasurehunt.user.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.treasurehunt.common.repository.criteria.CriteriaFilter;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UserCriteriaFilterImpl implements CriteriaFilter<UserSearchCriteria> {
    @Override
    public Predicate getPredicate(UserSearchCriteria searchCriteria, Root<?> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (isNotBlank(searchCriteria.username())) {
            predicates.add(
                    criteriaBuilder.like(root.get("username"), "%" + searchCriteria.username() + "%"));
        }

        if (isNotBlank(searchCriteria.email())) {
            predicates.add(
                    criteriaBuilder.like(root.get("email"), "%" + searchCriteria.email() + "%"));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
