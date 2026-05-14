package com.techeer.carpool.domain.notification.emitter;

import com.techeer.carpool.domain.notification.dto.NotificationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterRegistry {

    // 동시 접속 대비 ConcurrentHashMap 사용
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 타임아웃 30분
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        // 연결 직후 더미 이벤트 — 503 방지 (SSE 첫 응답 전 연결 끊김 방지)
        sendEvent(emitter, "connect", "connected");
        return emitter;
    }

    public void send(Long userId, NotificationPayload payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return; // 연결 없으면 무시
        sendEvent(emitter, "notification", payload);
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
