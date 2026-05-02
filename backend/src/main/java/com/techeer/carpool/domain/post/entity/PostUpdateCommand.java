package com.techeer.carpool.domain.post.entity;

import java.time.LocalDateTime;
import java.util.List;

public record PostUpdateCommand(
        String title,
        String departureLocation,
        Double departureLat,
        Double departureLng,
        String destinationLocation,
        Double destinationLat,
        Double destinationLng,
        LocalDateTime departureTime,
        int maxPassengers,
        String description,
        boolean autoAccept,
        PostStatus status,
        Integer price,
        List<String> tags
) {}
