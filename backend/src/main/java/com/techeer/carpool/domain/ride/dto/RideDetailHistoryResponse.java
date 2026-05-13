package com.techeer.carpool.domain.ride.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RideDetailHistoryResponse {

    private LocalDateTime departureTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String departureLocation;
    private Double departureLat;
    private Double departureLng;
    private String destinationLocation;
    private Double destinationLat;
    private Double destinationLng;

    private Integer price;

    private List<PassengerHistoryInfo> passengers;
}
