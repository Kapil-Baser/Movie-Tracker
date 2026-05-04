package com.example.movieapi.repository;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.MovieCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CollectionRepository extends JpaRepository<MovieCollection, Long> {

    Optional<MovieCollection> findByName(String name);

    @Query("SELECT mc.name FROM MovieCollection AS mc WHERE mc.id = :collectionId")
    String findNameById(Long collectionId);

    Optional<MovieCollection> findByIdAndOwner(Long id, AppUser owner);

    boolean existsByName(String name);

    @NativeQuery(value = "SELECT count(*) FROM MOVIE_COLLECTION WHERE OWNER_ID = ?1")
    int findNumberOfCollectionsByOwnerId(Long ownerId);

    boolean existsByNameAndOwnerId(String name, Long ownerId);

    @Query("SELECT c FROM MovieCollection c JOIN FETCH c.movies WHERE c.id = :id")
    Optional<MovieCollection> findByIdWithMovies(@Param("id") Long id);

    Optional<MovieCollection> findByOwnerIdAndName(Long ownerId, String name);

    List<MovieCollection> findByOwnerIdOrderByUpdatedAtDesc(Long ownerId); // For getting all collections by a user

    List<MovieCollection> findByOwnerIsNullOrderByName(); // Default system collections

    List<MovieCollection> findByOwner(AppUser owner);

    List<MovieCollection> findByOwnerId(Long ownerId);

    Page<MovieCollection> findByOwner(AppUser owner, Pageable pageable);

    // Get all movie IDs in a collection
    @Query("SELECT m.id FROM MovieCollection mc JOIN mc.movies m WHERE mc.id = :collectionId")
    Set<Long> findAllMovieIdsInCollection(@Param("collectionId") Long collectionId);

    @Query("SELECT m.id FROM MovieCollection mc JOIN mc.movies m " +
            "WHERE mc.owner = :owner AND mc.name = :collectionName")
    Set<Long> findAllMovieIdsByOwnerAndName(@Param("owner") AppUser owner,
                                            @Param("collectionName") String collectionName);

    void deleteByOwnerAndId(AppUser owner, Long collectionId);
}
