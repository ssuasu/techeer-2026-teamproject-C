package com.techeer.carpool;

import com.techeer.carpool.domain.auth.repository.BlacklistRedisRepository;
import com.techeer.carpool.domain.auth.repository.RefreshTokenRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CarpoolApplicationTests {

	@MockBean RefreshTokenRedisRepository refreshTokenRedisRepository;
	@MockBean BlacklistRedisRepository blacklistRedisRepository;

	@Test
	void contextLoads() {
	}

}
