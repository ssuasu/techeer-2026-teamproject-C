package com.techeer.carpool.domain.ride.review.dto;

import com.techeer.carpool.domain.ride.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long id;
    private Long rideId;
    private Long reviewerId;
    private Long driverId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rideId(review.getRideId())
                .reviewerId(review.getReviewerId())
                .driverId(review.getDriverId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
