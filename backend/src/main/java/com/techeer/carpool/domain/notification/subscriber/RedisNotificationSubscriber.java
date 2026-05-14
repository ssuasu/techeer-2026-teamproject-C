package com.techeer.carpool.domain.notification.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techeer.carpool.domain.notification.dto.NotificationPayload;
import com.techeer.carpool.domain.notification.emitter.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationSubscriber {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;

    // RedisMessageListenerContainer가 "notification:{userId}" 채널 메시지 수신 시 호출
    public void onMessage(String message, String channel) {
        try {
            Long userId = Long.parseLong(channel.replace("notification:", ""));
            NotificationPayload payload = objectMapper.readValue(message, NotificationPayload.class);
            sseEmitterRegistry.send(userId, payload);
        } catch (JsonProcessingException e) {
            // 역직렬화 실패 시 해당 메시지만 스킵, 다른 구독자에게 영향 없음
            log.error("알림 메시지 역직렬화 실패: channel={}", channel, e);
        }
    }
}
