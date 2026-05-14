package com.techeer.carpool.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

    private static final String KEY_PREFIX = "refresh:member:";
    private static final long TTL_SECONDS = 604800L; // 7일

    private final StringRedisTemplate redisTemplate;

    public void save(Long memberId, String token) {
        redisTemplate.opsForValue().set(KEY_PREFIX + memberId, token, TTL_SECONDS, TimeUnit.SECONDS);
    }

    public Optional<String> findByMemberId(Long memberId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + memberId));
    }

    public void delete(Long memberId) {
        redisTemplate.delete(KEY_PREFIX + memberId);
    }
}
