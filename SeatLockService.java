package com.takehome.moviebooking.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatLockService {
    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "seat-hold-delay-queue";

    public boolean acquireLock(UUID showId, UUID seatId, UUID userId, long durationMinutes) {
        String lockKey = "seat-lock:" + showId + ":" + seatId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, userId.toString(), Duration.ofMinutes(durationMinutes));
        if (Boolean.TRUE.equals(acquired)) {
            redisTemplate.opsForZSet().add(QUEUE_KEY, holdKey(showId, seatId), System.currentTimeMillis() + (durationMinutes * 60 * 1000));
            return true;
        }
        return false;
    }

    public void releaseLock(String holdKey) {
        redisTemplate.delete(holdKey);
        redisTemplate.opsForZSet().remove(QUEUE_KEY, holdKey);
    }

    public Set<String> pollExpiredLocks() {
        return redisTemplate.opsForZSet().rangeByScore(QUEUE_KEY, 0, System.currentTimeMillis());
    }

    private String holdKey(UUID showId, UUID seatId) { return "seat-lock:" + showId + ":" + seatId; }
}