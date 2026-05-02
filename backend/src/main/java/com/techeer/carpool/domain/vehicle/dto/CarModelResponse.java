package com.techeer.carpool.domain.vehicle.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarModelResponse {

    private String brand;
    private String model;
}