package com.techeer.carpool.domain.driver.service;

import com.techeer.carpool.domain.driver.dto.DriverRegisterRequest;
import com.techeer.carpool.domain.driver.dto.DriverResponse;
import com.techeer.carpool.domain.driver.entity.Driver;
import com.techeer.carpool.domain.driver.repository.DriverRepository;
import com.techeer.carpool.domain.member.repository.MemberRepository;
import com.techeer.carpool.domain.vehicle.repository.CarColorRepository;
import com.techeer.carpool.domain.vehicle.repository.CarModelRepository;
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
    private final CarModelRepository carModelRepository;
    private final CarColorRepository carColorRepository;

    @Transactional
    public DriverResponse registerDriver(Long memberId, DriverRegisterRequest request) {
        memberRepository.findByIdAndDeletedFalse(memberId)
                .orElseThrow(() -> new CarpoolException(ErrorCode.MEMBER_NOT_FOUND));

        if (driverRepository.findByMemberIdAndDeletedFalse(memberId).isPresent()) {
            throw new CarpoolException(ErrorCode.DRIVER_ALREADY_REGISTERED);
        }

        carModelRepository.findById(request.getCarModelId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.CAR_MODEL_NOT_FOUND));

        carColorRepository.findById(request.getCarColorId())
                .orElseThrow(() -> new CarpoolException(ErrorCode.CAR_COLOR_NOT_FOUND));

        Driver driver = Driver.builder()
                .memberId(memberId)
                .carModelId(request.getCarModelId())
                .carColorId(request.getCarColorId())
                .carNumber(request.getCarNumber())
                .build();

        return DriverResponse.from(driverRepository.save(driver));
    }
}
