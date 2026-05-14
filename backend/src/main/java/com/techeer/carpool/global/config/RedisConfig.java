package com.techeer.carpool.global.config;

import com.techeer.carpool.domain.notification.subscriber.RedisNotificationSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // StringRedisTemplate은 Spring Boot 자동 구성 사용 (별도 빈 불필요)

    // 알림 페이로드 직렬화용 (JSON)
    @Bean
    public RedisTemplate<String, Object> notificationRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // "notification:*" 패턴 구독 — 메시지 수신 시 RedisNotificationSubscriber.onMessage() 호출
    @Bean
    public RedisMessageListenerContainer listenerContainer(
            RedisConnectionFactory factory,
            RedisNotificationSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(
                new MessageListenerAdapter(subscriber, "onMessage"),
                new PatternTopic("notification:*")
        );
        return container;
    }
}
