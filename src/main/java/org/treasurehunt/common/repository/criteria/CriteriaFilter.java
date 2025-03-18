package org.treasurehunt.common.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * An interface for defining dynamic filtering criteria used in JPA Criteria API queries.
 * <p>
 * Implementations of this interface provide a method to generate a {@link Predicate}
 * based on the provided search criteria, which can be applied to filter query results dynamically.
 * </p>
 *
 * @param <T> The type of the search criteria object.
 */
public interface CriteriaFilter<T> {

    /**
     * Generates a {@link Predicate} based on the given search criteria.
     *
     * @param searchCriteria  The filtering criteria used to generate the predicate.
     * @param root            The root entity in the query.
     * @param criteriaBuilder The {@link CriteriaBuilder} used to construct the predicate.
     * @return A {@link Predicate} representing the filtering conditions.
     */
    Predicate getPredicate(T searchCriteria, Root<?> root, CriteriaBuilder criteriaBuilder);
}
