package com.baseball.ticket.global.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisInitializer implements ApplicationRunner {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // 서버 시작 시 좌석 선점 키 전부 초기화
        Set<String> seatLockKeys = redisTemplate.keys("seat_lock:*");
        if (seatLockKeys != null && !seatLockKeys.isEmpty()) {
            redisTemplate.delete(seatLockKeys);
        }

        Set<String> lockTokenKeys = redisTemplate.keys("lock_token:*");
        if (lockTokenKeys != null && !lockTokenKeys.isEmpty()) {
            redisTemplate.delete(lockTokenKeys);
        }

        System.out.println("✅ Redis 좌석 선점 키 초기화 완료");
    }
}