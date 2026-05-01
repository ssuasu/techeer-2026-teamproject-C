package com.techeer.carpool.domain.driver.service;

import com.techeer.carpool.domain.driver.dto.DriverResponse;
import com.techeer.carpool.domain.driver.dto.DriverUpdateRequest;
import com.techeer.carpool.domain.driver.entity.Driver;
import com.techeer.carpool.domain.driver.repository.DriverRepository;
import com.techeer.carpool.domain.vehicle.entity.VehicleOption;
import com.techeer.carpool.domain.vehicle.entity.VehicleOptionType;
import com.techeer.carpool.domain.vehicle.repository.VehicleOptionRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverUpdateService {

    private final DriverRepository driverRepository;
    private final VehicleOptionRepository vehicleOptionRepository;

    @Transactional
    public DriverResponse updateDriver(Long memberId, DriverUpdateRequest request) {
        Driver driver = driverRepository.findByMemberIdAndDeletedFalse(memberId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.DRIVER_NOT_FOUND));

        if (!driver.getCarNumber().equals(request.getCarNumber())
                && driverRepository.existsByCarNumber(request.getCarNumber())) {
            throw new CarpoolException(ErrorCode.CAR_NUMBER_DUPLICATE);
        }

        VehicleOption carModel = vehicleOptionRepository.findById(request.getCarModelId())
                .filter(o -> o.getType() == VehicleOptionType.MODEL)
                .orElseThrow(() -> new CarpoolException(ErrorCode.CAR_MODEL_NOT_FOUND));

        VehicleOption carColor = vehicleOptionRepository.findById(request.getCarColorId())
                .filter(o -> o.getType() == VehicleOptionType.COLOR)
                .orElseThrow(() -> new CarpoolException(ErrorCode.CAR_COLOR_NOT_FOUND));

        driver.update(request.getCarModelId(), request.getCarColorId(), request.getCarNumber());

        return DriverResponse.from(driver, carModel, carColor);
    }
}
