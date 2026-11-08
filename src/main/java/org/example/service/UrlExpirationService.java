package org.example.service;

import org.example.repository.UrlRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlExpirationService {
    private final UrlRepository urlRepository;
    private final ScheduledExecutorService scheduler;

    public UrlExpirationService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                urlRepository::deleteExpiredUrls,
                1,
                1,
                TimeUnit.MINUTES
        );
    }

    public void stop() {
        scheduler.shutdown();
    }
}
