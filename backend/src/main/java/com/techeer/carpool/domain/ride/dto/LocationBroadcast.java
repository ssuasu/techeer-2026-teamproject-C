package com.techeer.carpool.domain.ride.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationBroadcast {
    private Long rideId;
    private Double latitude;
    private Double longitude;
    private String timestamp;
}
