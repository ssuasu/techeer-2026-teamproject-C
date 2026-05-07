package com.techeer.carpool.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DriverRatingResponse {

    private Long driverId;
    private Double averageRating;
    private long totalCount;

    public static DriverRatingResponse of(Long driverId, Double avg, long count) {
        return DriverRatingResponse.builder()
                .driverId(driverId)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                .totalCount(count)
                .build();
    }
}
