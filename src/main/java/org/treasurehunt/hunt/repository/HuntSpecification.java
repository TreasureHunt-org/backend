package org.treasurehunt.hunt.repository;

import org.springframework.data.jpa.domain.Specification;
import org.treasurehunt.hunt.api.HuntFilter;
import org.treasurehunt.hunt.repository.entity.Hunt;

public class HuntSpecification {

    public static Specification<Hunt> getSpecification(HuntFilter filter) {
        return Specification.where(titleContains(filter.getTitle()))
                .and(statusEquals(filter.getStatus()))
                .and(organizerIdEquals(filter.getOrganizerId()))
                .and(startDateGreaterThanOrEqual(filter.getStartDateFrom()))
                .and(startDateLessThanOrEqual(filter.getStartDateTo()))
                .and(endDateGreaterThanOrEqual(filter.getEndDateFrom()))
                .and(endDateLessThanOrEqual(filter.getEndDateTo()));
    }

    private static Specification<Hunt> titleContains(String title) {
        return (title == null || title.isEmpty()) ? null :
                (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private static Specification<Hunt> statusEquals(org.treasurehunt.common.enums.HuntStatus status) {
        return status == null ? null :
                (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Hunt> organizerIdEquals(Long organizerId) {
        return organizerId == null ? null :
                (root, query, cb) -> cb.equal(root.get("organizer").get("id"), organizerId);
    }

    private static Specification<Hunt> startDateGreaterThanOrEqual(java.time.Instant startDate) {
        return startDate == null ? null :
                (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    private static Specification<Hunt> startDateLessThanOrEqual(java.time.Instant startDate) {
        return startDate == null ? null :
                (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), startDate);
    }

    private static Specification<Hunt> endDateGreaterThanOrEqual(java.time.Instant endDate) {
        return endDate == null ? null :
                (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), endDate);
    }

    private static Specification<Hunt> endDateLessThanOrEqual(java.time.Instant endDate) {
        return endDate == null ? null :
                (root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), endDate);
    }
}