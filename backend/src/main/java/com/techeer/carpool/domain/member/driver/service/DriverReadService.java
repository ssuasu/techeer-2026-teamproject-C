package com.techeer.carpool.domain.member.driver.service;

import com.techeer.carpool.domain.member.driver.dto.DriverResponse;
import com.techeer.carpool.domain.member.driver.entity.Driver;
import com.techeer.carpool.domain.member.driver.repository.DriverRepository;
import com.techeer.carpool.domain.member.vehicle.entity.VehicleOption;
import com.techeer.carpool.domain.member.vehicle.repository.VehicleOptionRepository;
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

        VehicleOption vehicleOption = vehicleOptionRepository.findById(driver.getVehicleOptionId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.VEHICLE_OPTION_NOT_FOUND));

        return DriverResponse.from(driver, vehicleOption);
    }
}
