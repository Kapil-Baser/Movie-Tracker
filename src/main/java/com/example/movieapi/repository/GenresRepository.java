package com.example.movieapi.repository;

import com.example.movieapi.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenresRepository extends JpaRepository<Genre, Integer> {

    Optional<Genre> findByName(String name);
}
