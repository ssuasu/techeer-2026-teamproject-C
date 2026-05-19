package com.techeer.carpool.domain.driver.controller;

import com.techeer.carpool.domain.driver.dto.CarColorResponse;
import com.techeer.carpool.domain.driver.dto.CarModelResponse;
import com.techeer.carpool.domain.driver.dto.DriverRegisterRequest;
import com.techeer.carpool.domain.driver.dto.DriverResponse;
import com.techeer.carpool.domain.driver.dto.DriverUpdateRequest;
import com.techeer.carpool.domain.driver.service.DriverDeleteService;
import com.techeer.carpool.domain.driver.service.DriverReadService;
import com.techeer.carpool.domain.driver.service.DriverRegisterService;
import com.techeer.carpool.domain.driver.service.DriverUpdateService;
import com.techeer.carpool.domain.driver.service.VehicleReadService;
import com.techeer.carpool.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRegisterService driverRegisterService;
    private final DriverReadService driverReadService;
    private final DriverUpdateService driverUpdateService;
    private final DriverDeleteService driverDeleteService;
    private final VehicleReadService vehicleReadService;

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> registerDriver(
            @Valid @RequestBody DriverRegisterRequest request,
            Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        DriverResponse response = driverRegisterService.registerDriver(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("운전자 등록이 완료되었습니다.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DriverResponse>> getMyDriver(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        DriverResponse response = driverReadService.getMyDriver(memberId);
        return ResponseEntity.ok(ApiResponse.of("운전자 정보 조회 성공", response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @Valid @RequestBody DriverUpdateRequest request,
            Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        DriverResponse response = driverUpdateService.updateDriver(memberId, request);
        return ResponseEntity.ok(ApiResponse.of("운전자 정보가 수정되었습니다.", response));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteDriver(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        driverDeleteService.deleteDriver(memberId);
        return ResponseEntity.noContent().build();
    }

    // ── Vehicle Options (드라이버 등록 폼용 조회) ─────────────────────────────

    @GetMapping("/vehicles/models")
    public ResponseEntity<ApiResponse<List<CarModelResponse>>> getVehicleModels() {
        return ResponseEntity.ok(ApiResponse.of("차량 모델 목록 조회 성공",
                vehicleReadService.getModels()));
    }

    @GetMapping("/vehicles/colors")
    public ResponseEntity<ApiResponse<List<CarColorResponse>>> getVehicleColors(
            @RequestParam String brand,
            @RequestParam String model) {
        return ResponseEntity.ok(ApiResponse.of("차량 색상 목록 조회 성공",
                vehicleReadService.getColors(brand, model)));
    }
}
