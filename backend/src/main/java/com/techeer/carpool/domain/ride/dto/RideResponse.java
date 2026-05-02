package com.techeer.carpool.domain.ride.dto;

import com.techeer.carpool.domain.ride.entity.Ride;
import com.techeer.carpool.domain.ride.entity.RideStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RideResponse {

    private Long id;
    private Long postId;
    private Long driverId;
    private RideStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public static RideResponse from(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .postId(ride.getPostId())
                .driverId(ride.getDriverId())
                .status(ride.getStatus())
                .currentLatitude(ride.getCurrentLatitude())
                .currentLongitude(ride.getCurrentLongitude())
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .build();
    }
}
