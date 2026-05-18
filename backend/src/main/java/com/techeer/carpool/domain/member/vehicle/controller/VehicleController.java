package com.techeer.carpool.domain.member.vehicle.controller;

import com.techeer.carpool.domain.member.vehicle.dto.CarColorResponse;
import com.techeer.carpool.domain.member.vehicle.dto.CarModelResponse;
import com.techeer.carpool.domain.member.vehicle.service.VehicleReadService;
import com.techeer.carpool.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleReadService vehicleReadService;

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<CarModelResponse>>> getModels() {
        return ResponseEntity.ok(ApiResponse.of("차량 모델 목록 조회 성공", vehicleReadService.getModels()));
    }

    @GetMapping("/colors")
    public ResponseEntity<ApiResponse<List<CarColorResponse>>> getColors(
            @RequestParam String brand,
            @RequestParam String model) {
        return ResponseEntity.ok(ApiResponse.of("차량 색상 목록 조회 성공", vehicleReadService.getColors(brand, model)));
    }
}
