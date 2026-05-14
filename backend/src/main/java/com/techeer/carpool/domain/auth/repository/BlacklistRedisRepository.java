package com.techeer.carpool.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class BlacklistRedisRepository {

    private static final String KEY_PREFIX = "blacklist:";

    private final StringRedisTemplate redisTemplate;

    // 이미 만료된 토큰은 등록 불필요
    public void add(String token, long remainingSeconds) {
        if (remainingSeconds <= 0) return;
        redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", remainingSeconds, TimeUnit.SECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + token));
    }
}
