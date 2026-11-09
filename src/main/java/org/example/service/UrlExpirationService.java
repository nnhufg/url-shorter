
package org.example.service;

import org.example.repository.UrlRepository;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UrlExpirationService {

    private final UrlRepository urlRepository;
    private final UrlPersistenceService urlPersistenceService;
    private final ScheduledExecutorService scheduler;
    private final int cleanupIntervalMinutes;
    private final BlockingQueue<String> notifications =
            new LinkedBlockingQueue<>();

    private boolean started = false;

    public UrlExpirationService(
            UrlRepository urlRepository,
            UrlPersistenceService urlPersistenceService,
            int cleanupIntervalMinutes
    ) {

        if (cleanupIntervalMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Интервал очистки должен быть больше нуля"
            );
        }

        this.urlRepository = urlRepository;
        this.urlPersistenceService = urlPersistenceService;
        this.cleanupIntervalMinutes = cleanupIntervalMinutes;

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized void start() {

        if (started || scheduler.isShutdown()) {
            return;
        }

        started = true;

        scheduler.scheduleWithFixedDelay(
                this::cleanupExpiredUrls,
                cleanupIntervalMinutes,
                cleanupIntervalMinutes,
                TimeUnit.MINUTES
        );
    }

    private void cleanupExpiredUrls() {

        try {

            boolean deleted = urlRepository.deleteExpiredUrls();

            if (deleted) {
                urlPersistenceService.saveUrls();

                notifications.offer(
                        "Просроченные ссылки удалены. JSON обновлён."
                );
            }

        } catch (IOException e) {

            notifications.offer(
                    "Ошибка сохранения JSON после очистки TTL: "
                            + e.getMessage()
            );
        }
    }

    public void stop() {

        scheduler.shutdown();

        try {

            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

        } catch (InterruptedException e) {

            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String getNotification() {
        return notifications.poll();
    }
}
