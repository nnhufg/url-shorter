package org.example.service;

import org.example.repository.UrlRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlExpirationService {
    private final UrlRepository urlRepository;
    private final ScheduledExecutorService scheduler;
    private final int cleanupIntervalMinutes;

    public UrlExpirationService(
            UrlRepository urlRepository,
            int cleanupIntervalMinutes
    ) {
        this.urlRepository = urlRepository;
        this.cleanupIntervalMinutes = cleanupIntervalMinutes;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                urlRepository::deleteExpiredUrls,
                cleanupIntervalMinutes,
                cleanupIntervalMinutes,
                TimeUnit.MINUTES
        );
    }

    public void stop() {
        scheduler.shutdown();
    }
}
