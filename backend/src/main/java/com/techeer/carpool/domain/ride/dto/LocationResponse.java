package com.techeer.carpool.domain.ride.dto;

import com.techeer.carpool.domain.ride.entity.Ride;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationResponse {

    private Long rideId;
    private Double driverLatitude;
    private Double driverLongitude;

    public static LocationResponse from(Ride ride) {
        return LocationResponse.builder()
                .rideId(ride.getId())
                .driverLatitude(ride.getCurrentLatitude())
                .driverLongitude(ride.getCurrentLongitude())
                .build();
    }
}
