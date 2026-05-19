package com.techeer.carpool.domain.driver.service;

import com.techeer.carpool.domain.driver.dto.CarColorResponse;
import com.techeer.carpool.domain.driver.dto.CarModelResponse;
import com.techeer.carpool.domain.driver.repository.VehicleOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleReadService {

    private final VehicleOptionRepository vehicleOptionRepository;

    @Transactional(readOnly = true)
    public List<CarModelResponse> getModels() {
        return vehicleOptionRepository.findDistinctBrandAndModel().stream()
                .map(row -> CarModelResponse.builder()
                        .brand((String) row[0])
                        .model((String) row[1])
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CarColorResponse> getColors(String brand, String model) {
        return vehicleOptionRepository.findByBrandAndModelOrderByColorAsc(brand, model).stream()
                .map(CarColorResponse::from)
                .toList();
    }
}
