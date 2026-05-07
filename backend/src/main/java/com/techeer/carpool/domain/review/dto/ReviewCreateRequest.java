package com.techeer.carpool.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewCreateRequest {

    @NotNull
    private Long rideId;

    @NotNull
    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
}
