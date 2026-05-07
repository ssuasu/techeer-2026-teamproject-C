package com.techeer.carpool.domain.review.service;

import com.techeer.carpool.domain.review.dto.DriverRatingResponse;
import com.techeer.carpool.domain.review.dto.ReviewCreateRequest;
import com.techeer.carpool.domain.review.dto.ReviewResponse;
import com.techeer.carpool.domain.review.entity.Review;
import com.techeer.carpool.domain.review.repository.ReviewRepository;
import com.techeer.carpool.domain.ride.entity.Ride;
import com.techeer.carpool.domain.ride.entity.RideStatus;
import com.techeer.carpool.domain.ride.repository.RideRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RideRepository rideRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, Long reviewerId) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.RIDE_NOT_FOUND));

        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new CarpoolException(ErrorCode.REVIEW_RIDE_NOT_COMPLETED);
        }

        boolean isPassenger = ride.getPassengers().stream()
                .anyMatch(p -> p.getPassengerId().equals(reviewerId));
        if (!isPassenger) {
            throw new CarpoolException(ErrorCode.REVIEW_FORBIDDEN);
        }

        if (reviewRepository.existsByRideIdAndReviewerId(request.getRideId(), reviewerId)) {
            throw new CarpoolException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = reviewRepository.save(Review.builder()
                .rideId(request.getRideId())
                .reviewerId(reviewerId)
                .driverId(ride.getDriverId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build());

        return ReviewResponse.from(review);
    }

    public Optional<ReviewResponse> getMyReviewForRide(Long rideId, Long reviewerId) {
        return reviewRepository.findByRideIdAndReviewerId(rideId, reviewerId)
                .map(ReviewResponse::from);
    }

    public DriverRatingResponse getDriverRating(Long driverId) {
        Double avg = reviewRepository.findAverageRatingByDriverId(driverId);
        long count = reviewRepository.countByDriverId(driverId);
        return DriverRatingResponse.of(driverId, avg, count);
    }
}
