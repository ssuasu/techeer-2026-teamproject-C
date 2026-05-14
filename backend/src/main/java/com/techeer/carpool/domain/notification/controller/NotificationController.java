package com.techeer.carpool.domain.notification.controller;

import com.techeer.carpool.domain.notification.emitter.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return sseEmitterRegistry.subscribe(memberId);
    }
}
