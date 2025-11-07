package com.example.movieapi.repository;

import com.example.movieapi.entity.MovieCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<MovieCollection, Long> {

    Optional<MovieCollection> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM MovieCollection c JOIN FETCH c.movies WHERE c.id = :id")
    Optional<MovieCollection> findByIdWithMovies(@Param("id") Long id);
}
