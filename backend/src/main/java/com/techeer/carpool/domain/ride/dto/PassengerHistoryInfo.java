package com.techeer.carpool.domain.ride.dto;

import com.techeer.carpool.domain.ride.entity.PassengerStatus;
import com.techeer.carpool.domain.ride.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PassengerHistoryInfo {

    private Long passengerId;
    private String nickname;
    private PassengerStatus status;
    private LocalDateTime boardedAt;
    private LocalDateTime droppedOffAt;
    private PaymentStatus paymentStatus;
    private boolean hasReviewed;
}
