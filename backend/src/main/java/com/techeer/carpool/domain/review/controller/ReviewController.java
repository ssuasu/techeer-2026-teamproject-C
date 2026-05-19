package com.techeer.carpool.domain.review.controller;

import com.techeer.carpool.domain.review.dto.ReviewCreateRequest;
import com.techeer.carpool.domain.review.dto.ReviewResponse;
import com.techeer.carpool.domain.review.service.ReviewCreateService;
import com.techeer.carpool.domain.review.service.ReviewQueryService;
import com.techeer.carpool.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCreateService reviewCreateService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping("/rides/{rideId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long rideId,
            @RequestBody @Valid ReviewCreateRequest request,
            Authentication authentication) {
        Long reviewerId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("평가가 등록되었습니다.", reviewCreateService.createReview(rideId, request, reviewerId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getMyReviews(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.of("내 평가 목록 조회 성공", reviewQueryService.getMyReviews(memberId)));
    }

    @GetMapping("/ride/{rideId}/me")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReviewForRide(
            @PathVariable Long rideId,
            Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.of("내 평가 조회 성공",
                reviewQueryService.getMyReviewForRide(rideId, memberId).orElse(null)));
    }

    @GetMapping("/driver/{memberId}/rating")
    public ResponseEntity<ApiResponse<Double>> getDriverRating(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.of("드라이버 평점 조회 성공", reviewQueryService.getDriverRating(memberId)));
    }
}
