package com.innowise.orderservice.repository.specification;

import com.innowise.orderservice.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> hasStatuses(List<String> statuses) {
        return (root, query, cb) -> statuses == null || statuses.isEmpty() ?
                cb.conjunction() : root.get("status").in(statuses);
    }

    public static Specification<Order> createdBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return cb.conjunction();
            }

            if (from != null && to != null) {
                return cb.between(root.get("createdAt"), from, to);
            }

            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

}
