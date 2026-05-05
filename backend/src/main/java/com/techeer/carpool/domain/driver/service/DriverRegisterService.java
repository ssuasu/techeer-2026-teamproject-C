package com.techeer.carpool.domain.driver.service;

import com.techeer.carpool.domain.driver.dto.DriverRegisterRequest;
import com.techeer.carpool.domain.driver.dto.DriverResponse;
import com.techeer.carpool.domain.driver.entity.Driver;
import com.techeer.carpool.domain.driver.repository.DriverRepository;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.vehicle.entity.VehicleOption;
import com.techeer.carpool.domain.vehicle.repository.VehicleOptionRepository;
import com.techeer.carpool.global.exception.CarpoolException;
import com.techeer.carpool.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverRegisterService {

    private final DriverRepository driverRepository;
    private final MemberRepository memberRepository;
    private final VehicleOptionRepository vehicleOptionRepository;

    @Transactional
    public DriverResponse registerDriver(Long memberId, DriverRegisterRequest request) {
        memberRepository.findByIdAndDeletedFalse(memberId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.MEMBER_NOT_FOUND));

        if (driverRepository.findByMemberIdAndDeletedFalse(memberId).isPresent()) {
            throw new CarpoolException(ErrorCode.DRIVER_ALREADY_REGISTERED);
        }

        if (driverRepository.existsByCarNumberAndDeletedFalse(request.getCarNumber())) {
            throw new CarpoolException(ErrorCode.CAR_NUMBER_DUPLICATE);
        }

        VehicleOption vehicleOption = vehicleOptionRepository.findById(request.getVehicleOptionId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.VEHICLE_OPTION_NOT_FOUND));

        Driver driver = Driver.builder()
                .memberId(memberId)
                .vehicleOptionId(request.getVehicleOptionId())
                .carNumber(request.getCarNumber())
                .build();

        return DriverResponse.from(driverRepository.save(driver), vehicleOption);
    }
}
