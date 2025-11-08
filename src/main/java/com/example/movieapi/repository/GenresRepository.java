package com.example.movieapi.repository;

import com.example.movieapi.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GenresRepository extends JpaRepository<Genre, Integer> {

    Optional<Genre> findByName(String name);

    Set<Genre> findByIdIn(Set<Integer> genreIds);
}
