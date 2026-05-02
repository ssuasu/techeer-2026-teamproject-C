package com.techeer.carpool.global.exception;

// Websocket 테스트용 시작
import lombok.extern.slf4j.Slf4j;
// 끝

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j   // 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CarpoolException.class)
    public ResponseEntity<Map<String, String>> handleCarpoolException(CarpoolException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of("code", errorCode.getCode(), "message", errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("잘못된 입력값입니다.");
        return ResponseEntity
                .badRequest()
                .body(Map.of("code", "COMMON_001", "message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        // 시작
        log.error("예상치 못한 서버 오류 발생", e);
        // 끝
        return ResponseEntity
                .status(500)
                .body(Map.of("code", "SERVER_ERROR", "message", "서버 오류가 발생했습니다."));
    }
}
