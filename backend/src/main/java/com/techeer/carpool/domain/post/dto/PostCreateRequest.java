package com.techeer.carpool.domain.post.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "출발지는 필수입니다.")
    private String departureLocation;

    private Double departureLat;
    private Double departureLng;

    @NotBlank(message = "목적지는 필수입니다.")
    private String destinationLocation;

    private Double destinationLat;
    private Double destinationLng;

    @NotNull(message = "출발 시간은 필수입니다.")
    @Future(message = "출발 시간은 현재 이후여야 합니다.")
    private LocalDateTime departureTime;

    @Min(value = 1, message = "최대 탑승 인원은 1명 이상이어야 합니다.")
    private int maxPassengers;

    private String description;
    private boolean autoAccept;
    private Integer price;
    private List<Long> tagIds;
}
