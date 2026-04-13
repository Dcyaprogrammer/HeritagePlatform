package com.heritage.platform.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<LocalDateTime>> attempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS_PER_HOUR = 20;
    private static final int HOUR_IN_SECONDS = 3600;

    public boolean isAllowed(String ip) {
        LocalDateTime now = LocalDateTime.now();
        attempts.putIfAbsent(ip, new CopyOnWriteArrayList<>());

        CopyOnWriteArrayList<LocalDateTime> list = attempts.get(ip);

        list.removeIf(time -> time.isBefore(now.minusSeconds(HOUR_IN_SECONDS)));

        if (list.size() >= MAX_ATTEMPTS_PER_HOUR) {
            return false;
        }

        list.add(now);
        return true;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanOldAttempts() {
        LocalDateTime now = LocalDateTime.now();
        attempts.forEach((ip, list) -> {
            list.removeIf(time -> time.isBefore(now.minusSeconds(HOUR_IN_SECONDS)));
            if (list.isEmpty()) attempts.remove(ip);
        });
    }
}