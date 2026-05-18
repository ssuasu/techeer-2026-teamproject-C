package com.techeer.carpool.domain.member.driver.controller;

import com.techeer.carpool.domain.member.driver.dto.DriverRegisterRequest;
import com.techeer.carpool.domain.member.driver.dto.DriverResponse;
import com.techeer.carpool.domain.member.driver.dto.DriverUpdateRequest;
import com.techeer.carpool.domain.member.driver.service.DriverDeleteService;
import com.techeer.carpool.domain.member.driver.service.DriverReadService;
import com.techeer.carpool.domain.member.driver.service.DriverRegisterService;
import com.techeer.carpool.domain.member.driver.service.DriverUpdateService;
import com.techeer.carpool.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverRegisterService driverRegisterService;
    private final DriverReadService driverReadService;
    private final DriverUpdateService driverUpdateService;
    private final DriverDeleteService driverDeleteService;

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
}
