package com.techeer.carpool.domain.review.repository;

import com.techeer.carpool.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByRideIdAndReviewerId(Long rideId, Long reviewerId);

    boolean existsByRideIdAndReviewerId(Long rideId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.driverId = :driverId")
    Double findAverageRatingByDriverId(@Param("driverId") Long driverId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.driverId = :driverId")
    long countByDriverId(@Param("driverId") Long driverId);
}
