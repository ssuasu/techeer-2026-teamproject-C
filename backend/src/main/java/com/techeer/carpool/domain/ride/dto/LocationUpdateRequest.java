package com.techeer.carpool.domain.ride.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LocationUpdateRequest {

    @NotNull(message = "latitude는 필수입니다.")
    private Double latitude;

    @NotNull(message = "longitude는 필수입니다.")
    private Double longitude;
}
