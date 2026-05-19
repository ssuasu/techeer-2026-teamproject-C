package com.techeer.carpool.domain.review.repository;

import com.techeer.carpool.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByRideIdAndReviewerId(Long rideId, Long reviewerId);

    List<Review> findAllByRideId(Long rideId);

    boolean existsByRideIdAndReviewerId(Long rideId, Long reviewerId);

    List<Review> findAllByReviewerId(Long reviewerId);
}
