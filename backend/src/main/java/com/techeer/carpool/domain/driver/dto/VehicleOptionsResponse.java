package com.techeer.carpool.domain.driver.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VehicleOptionsResponse {

    private List<CarModelResponse> models;
    private List<CarColorResponse> colors;
}
