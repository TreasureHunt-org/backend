package org.treasurehunt.common.repository.criteria;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.treasurehunt.common.api.PageDTO;

import java.util.Objects;

/**
 * A generic repository for handling dynamic filtering, sorting, and pagination using JPA Criteria API.
 *
 * @param <T> The entity type.
 * @param <S> The search criteria type.
 */
public abstract class CriteriaRepository<T, S> {

    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaFilter<S> criteriaFilter;
    private final Class<T> entityClass;


    public CriteriaRepository(EntityManager entityManager,
                              CriteriaFilter<S> criteriaFilter,
                              Class<T> entityClass) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaFilter = criteriaFilter;
        this.entityClass = entityClass;
    }

    /**
     * Finds all entities matching the given search criteria with pagination and sorting.
     *
     * @param pageDTO        The pagination and sorting details.
     * @param searchCriteria The filtering criteria.
     * @return A paginated list of entities.
     */
    public Page<T> findAllWithFilters(PageDTO pageDTO, S searchCriteria) {
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);

        Predicate predicate = criteriaFilter.getPredicate(searchCriteria, root, criteriaBuilder);
        criteriaQuery.where(predicate);

        setOrder(pageDTO, criteriaQuery, root);

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(pageDTO.getPage() * pageDTO.getPageSize());
        typedQuery.setMaxResults(pageDTO.getPageSize());

        Pageable pageable = getPageable(pageDTO);
        Long entityCount = getEntityCount(searchCriteria);

        return new PageImpl<>(typedQuery.getResultList(), pageable, entityCount);
    }

    /**
     * Sets the sorting order dynamically based on the provided sorting criteria.
     * Supports sorting by nested fields for one level using the dot notation (e.g., "address.city").
     *
     * @param userPage      The pagination and sorting details.
     * @param criteriaQuery The JPA CriteriaQuery object.
     * @param root          The root entity in the query.
     */
    private void setOrder(PageDTO userPage, CriteriaQuery<T> criteriaQuery, Root<T> root) {
        String sortBy = userPage.getSortBy();
        Sort.Direction sortDirection = userPage.getSortDirection();

        if (sortBy == null || sortDirection == null) {
            return;
        }

        if (sortBy.contains(".")) {
            // Handle sorting by a nested field (e.g., "address.city")
            String[] fields = sortBy.split("\\.");
            if (fields.length != 2) {
                throw new IllegalArgumentException("Invalid nested sort format. Use 'relation.field' format.");
            }

            String parentEntity = fields[0];
            String childField = fields[1];

            // Ensure the parent entity is a valid relationship
            if (root.getModel().getAttributes().stream()
                    .noneMatch(attr -> attr.getName().equals(parentEntity) && attr.isAssociation())) {
                throw new IllegalArgumentException("Invalid relation name: " + parentEntity);
            }

            // Join the nested entity
            Join<T, ?> join = root.join(parentEntity);

            if (userPage.getSortDirection().equals(Sort.Direction.ASC)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(join.get(childField)));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(join.get(childField)));
            }
        } else {

            // Ensure the field exists before sorting
            if (root.getModel().getAttributes().stream()
                    .noneMatch(attr -> attr.getName().equals(sortBy))) {
                throw new IllegalArgumentException("Invalid sort field: " + sortBy);
            }

            if (userPage.getSortDirection().equals(Sort.Direction.ASC)) {
                criteriaQuery.orderBy(criteriaBuilder.asc(root.get(sortBy)));
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(root.get(sortBy)));
            }
        }
    }

    /**
     * Creates a Pageable object for pagination.
     *
     * @param pageDTO The pagination and sorting details.
     * @return A Pageable instance with sorting applied.
     */
    private Pageable getPageable(PageDTO pageDTO) {
        if (Objects.nonNull(pageDTO.getSortBy())) {
            Sort sort = Sort.by(pageDTO.getSortDirection(), pageDTO.getSortBy());
            return PageRequest.of(pageDTO.getPage(), pageDTO.getPageSize(), sort);
        }
        return PageRequest.of(pageDTO.getPage(), pageDTO.getPageSize());
    }

    /**
     * Retrieves the total count of entities matching the given search criteria.
     *
     * @param searchCriteria The filtering criteria.
     * @return The count of matching entities.
     */
    private Long getEntityCount(S searchCriteria) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        countQuery.select(criteriaBuilder.count(countRoot));

        Predicate countPredicate = criteriaFilter.getPredicate(searchCriteria, countRoot, criteriaBuilder);
        if (countPredicate != null) {
            countQuery.where(countPredicate);
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

}