package com.acciobuild.common.specification;

import com.acciobuild.common.dto.SearchRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable dynamic database query builder using JPA Specifications.
 * Converts SearchRequest parameters list into query predicates.
 */
public class GenericSpecification<T> implements Specification<T> {

    private final SearchRequest searchRequest;

    public GenericSpecification(SearchRequest searchRequest) {
        this.searchRequest = searchRequest;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchRequest != null && searchRequest.getFilters() != null) {
            for (SearchRequest.Filter filter : searchRequest.getFilters()) {
                switch (filter.getOperator().toLowerCase()) {
                    case "eq":
                        predicates.add(cb.equal(root.get(filter.getKey()), filter.getValue()));
                        break;
                    case "like":
                        predicates.add(cb.like(cb.lower(root.get(filter.getKey())), "%" + filter.getValue().toString().toLowerCase() + "%"));
                        break;
                    case "gt":
                        predicates.add(cb.greaterThan(root.get(filter.getKey()), filter.getValue().toString()));
                        break;
                    case "lt":
                        predicates.add(cb.lessThan(root.get(filter.getKey()), filter.getValue().toString()));
                        break;
                    case "in":
                        if (filter.getValue() instanceof List) {
                            predicates.add(root.get(filter.getKey()).in((List<?>) filter.getValue()));
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
