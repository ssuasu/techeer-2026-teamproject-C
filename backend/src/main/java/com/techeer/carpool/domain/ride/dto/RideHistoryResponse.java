package com.techeer.carpool.domain.ride.dto;

import com.techeer.carpool.domain.post.entity.Post;
import com.techeer.carpool.domain.ride.entity.Ride;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RideHistoryResponse {

    private Long rideId;
    private String role;
    private String title;
    private String departureLocation;
    private String destinationLocation;
    private LocalDateTime departureTime;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int passengerCount;
    private Integer price;

    public static RideHistoryResponse from(Ride ride, Post post, String role) {
        return RideHistoryResponse.builder()
                .rideId(ride.getId())
                .role(role)
                .title(post != null ? post.getTitle() : null)
                .departureLocation(post != null ? post.getDepartureLocation() : null)
                .destinationLocation(post != null ? post.getDestinationLocation() : null)
                .departureTime(post != null ? post.getDepartureTime() : null)
                .startedAt(ride.getStartedAt())
                .completedAt(ride.getCompletedAt())
                .passengerCount(ride.getPassengers().size())
                .price(post != null ? post.getPrice() : null)
                .build();
    }
}
