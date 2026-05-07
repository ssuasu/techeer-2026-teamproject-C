package com.techeer.carpool.domain.ride.controller;

import com.techeer.carpool.domain.ride.dto.LocationBroadcast;
import com.techeer.carpool.domain.ride.dto.LocationMessage;
import com.techeer.carpool.domain.ride.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class RideWebSocketController {

    private final RideService rideService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/ride/{rideId}/location")
    public void handleLocation(
            @DestinationVariable Long rideId,
            @Payload LocationMessage message,
            Principal principal) {
        if (principal == null) return;
        Long driverId = Long.parseLong(principal.getName());

        rideService.updateLocationDirect(rideId, message.getLatitude(), message.getLongitude(), driverId);

        messagingTemplate.convertAndSend("/topic/ride/" + rideId,
                LocationBroadcast.builder()
                        .rideId(rideId)
                        .latitude(message.getLatitude())
                        .longitude(message.getLongitude())
                        .timestamp(LocalDateTime.now().toString())
                        .build());
    }
}
