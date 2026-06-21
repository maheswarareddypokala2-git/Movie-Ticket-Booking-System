package com.takehome.moviebooking.redis;

import com.takehome.moviebooking.domain.SeatHold;
import com.takehome.moviebooking.repository.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisQueuePoller {

    private final SeatLockService lockService;
    private final SeatHoldRepository holdRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    public void processExpiredHolds() {
        Set<String> expiredLocks = lockService.pollExpiredLocks();
        if (expiredLocks == null) return;

        for (String lockKey : expiredLocks) {
            lockService.releaseLock(lockKey);
            log.info("Redis lock cleared for key: {}", lockKey);
            kafkaTemplate.send("seat-hold-expired", lockKey);
        }
    }
}
