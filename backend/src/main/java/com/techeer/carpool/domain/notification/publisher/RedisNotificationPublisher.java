package com.techeer.carpool.domain.notification.publisher;

import com.techeer.carpool.domain.notification.dto.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisNotificationPublisher {

    private static final String CHANNEL_PREFIX = "notification:";

    // notificationRedisTemplate: RedisConfig에서 등록한 RedisTemplate<String, Object>
    private final RedisTemplate<String, Object> notificationRedisTemplate;

    public void publish(Long userId, NotificationPayload payload) {
        notificationRedisTemplate.convertAndSend(CHANNEL_PREFIX + userId, payload);
    }

    public void publishToMany(List<Long> userIds, NotificationPayload payload) {
        userIds.forEach(userId -> publish(userId, payload));
    }
}
