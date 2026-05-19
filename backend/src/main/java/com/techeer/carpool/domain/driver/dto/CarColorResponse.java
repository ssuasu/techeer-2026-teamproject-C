package com.techeer.carpool.domain.driver.dto;

import com.techeer.carpool.domain.driver.entity.CarColor;
import com.techeer.carpool.domain.driver.entity.VehicleOption;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarColorResponse {

    private Long vehicleOptionId;
    private CarColor color;
    private String colorLabel;
    private String colorHexCode;

    public static CarColorResponse from(VehicleOption option) {
        return CarColorResponse.builder()
                .vehicleOptionId(option.getId())
                .color(option.getColor())
                .colorLabel(option.getColor().getLabel())
                .colorHexCode(option.getColor().getHexCode())
                .build();
    }
}
