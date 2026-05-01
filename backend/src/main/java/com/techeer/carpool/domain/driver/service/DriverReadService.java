package com.techeer.carpool.domain.driver.service;

import com.techeer.carpool.domain.driver.dto.DriverResponse;
import com.techeer.carpool.domain.driver.entity.Driver;
import com.techeer.carpool.domain.driver.repository.DriverRepository;
import com.techeer.carpool.domain.vehicle.entity.VehicleOption;
import com.techeer.carpool.domain.vehicle.repository.VehicleOptionRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverReadService {

    private final DriverRepository driverRepository;
    private final VehicleOptionRepository vehicleOptionRepository;

    @Transactional(readOnly = true)
    public DriverResponse getMyDriver(Long memberId) {
        Driver driver = driverRepository.findByMemberIdAndDeletedFalse(memberId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.DRIVER_NOT_FOUND));

        VehicleOption carModel = vehicleOptionRepository.findById(driver.getCarModelId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.CAR_MODEL_NOT_FOUND));
        VehicleOption carColor = vehicleOptionRepository.findById(driver.getCarColorId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.CAR_COLOR_NOT_FOUND));

        return DriverResponse.from(driver, carModel, carColor);
    }
}
