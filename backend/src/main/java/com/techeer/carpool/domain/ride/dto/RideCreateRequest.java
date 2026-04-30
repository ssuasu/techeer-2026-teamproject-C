package com.techeer.carpool.domain.ride.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RideCreateRequest {

    @NotNull(message = "postId는 필수입니다.")
    private Long postId;
}
