package com.techeer.carpool.domain.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class DriverRegisterRequest {

    @NotNull(message = "차량을 선택해주세요.")
    private Long vehicleOptionId;

    @NotBlank(message = "차량 번호를 입력해주세요.")
    private String carNumber;
}