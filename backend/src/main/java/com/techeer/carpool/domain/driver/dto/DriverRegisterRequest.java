package com.techeer.carpool.domain.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class DriverRegisterRequest {

    @NotNull(message = "차량을 선택해주세요.")
    private Long vehicleOptionId;

    @NotBlank(message = "차량 번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{2,3}[가-힣]\\d{4}$", message = "차량 번호 형식이 올바르지 않습니다. (예: 12가3456, 123가4567)")
    private String carNumber;
}
