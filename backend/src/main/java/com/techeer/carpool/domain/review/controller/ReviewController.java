package com.techeer.carpool.domain.review.controller;

import com.techeer.carpool.domain.review.dto.DriverRatingResponse;
import com.techeer.carpool.domain.review.dto.ReviewCreateRequest;
import com.techeer.carpool.domain.review.dto.ReviewResponse;
import com.techeer.carpool.domain.review.service.ReviewService;
import com.techeer.carpool.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @RequestBody @Valid ReviewCreateRequest request,
            Authentication authentication) {
        Long reviewerId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("평가가 등록되었습니다.", reviewService.createReview(request, reviewerId)));
    }

    @GetMapping("/ride/{rideId}/me")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(
            @PathVariable Long rideId,
            Authentication authentication) {
        Long reviewerId = (Long) authentication.getPrincipal();
        Optional<ReviewResponse> review = reviewService.getMyReviewForRide(rideId, reviewerId);
        return ResponseEntity.ok(ApiResponse.of("내 평가 조회 성공", review.orElse(null)));
    }

    @GetMapping("/driver/{driverId}/rating")
    public ResponseEntity<ApiResponse<DriverRatingResponse>> getDriverRating(@PathVariable Long driverId) {
        return ResponseEntity.ok(ApiResponse.of("드라이버 평점 조회 성공", reviewService.getDriverRating(driverId)));
    }
}
