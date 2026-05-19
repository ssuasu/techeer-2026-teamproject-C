package com.techeer.carpool.domain.driver.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarModelResponse {

    private String brand;
    private String model;
}
