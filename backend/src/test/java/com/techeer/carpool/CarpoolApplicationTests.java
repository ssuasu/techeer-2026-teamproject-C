package com.techeer.carpool;

import com.techeer.carpool.domain.auth.repository.BlacklistRedisRepository;
import com.techeer.carpool.domain.auth.repository.RefreshTokenRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;

@SpringBootTest
class CarpoolApplicationTests {

	@Mock RefreshTokenRedisRepository refreshTokenRedisRepository;
	@Mock BlacklistRedisRepository blacklistRedisRepository;
	@Mock com.techeer.carpool.domain.notification.publisher.RedisNotificationPublisher notificationPublisher;

	@Test
	void contextLoads() {
	}

}
