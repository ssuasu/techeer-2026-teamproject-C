package com.techeer.carpool.domain.driver.dto;

import com.techeer.carpool.domain.driver.entity.Driver;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DriverResponse {

    private Long driverId;
    private Long memberId;
    private Long carModelId;
    private Long carColorId;
    private String carNumber;
    private LocalDateTime createdAt;

    public static DriverResponse from(Driver driver) {
        return DriverResponse.builder()
                .driverId(driver.getDriverId())
                .memberId(driver.getMemberId())
                .carModelId(driver.getCarModelId())
                .carColorId(driver.getCarColorId())
                .carNumber(driver.getCarNumber())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}
