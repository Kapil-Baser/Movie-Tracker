package com.example.movieapi.specification;

import com.example.movieapi.entity.CollectionType;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieCollection;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;

public class MovieSpecifications {
    private MovieSpecifications() {}

    public static Specification<Movie> hasTitle(String title) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("title"), title));
    }

    public static PredicateSpecification<Movie> hasTitlePredicate(String title) {
        return (from, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(from.get("title")), "%" + title + "%");
    }

    public static Specification<Movie> belongsToCollection(CollectionType collectionType) {
        return (root, query, criteriaBuilder) -> {
            Join<Movie, MovieCollection> collectionJoin = root.join("collections");
            return criteriaBuilder.equal(collectionJoin.get("type"), collectionType);
        };
    }
}
