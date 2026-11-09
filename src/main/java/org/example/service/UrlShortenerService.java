
package org.example.service;

import org.example.models.Url;
import org.example.repository.UrlRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.awt.Desktop;
import java.net.URI;

public class UrlShortenerService {

    private final UrlRepository urlRepository;
    private final UrlPersistenceService urlPersistenceService;

    public UrlShortenerService(
            UrlRepository urlRepository,
            UrlPersistenceService urlPersistenceService
    ) {
        this.urlRepository = urlRepository;
        this.urlPersistenceService = urlPersistenceService;
    }

    // Создание короткой ссылки
    public String shorten(
            UUID ownerId,
            String baseUrl,
            int clickLimit,
            Instant expiresAt
    ) throws IOException {

        String shortUrl;

        do {
            shortUrl = UrlShortener.generateShortUrl();
        } while (urlRepository.ifExists(shortUrl));

        Url url = new Url(
                ownerId,
                shortUrl,
                baseUrl,
                clickLimit,
                expiresAt
        );

        urlRepository.save(url);

        urlPersistenceService.saveUrls();

        return shortUrl;
    }

    public String getOriginalUrl(String shortUrl) {

        Url url = urlRepository.findByShortUrl(shortUrl);

        if (url == null) {
            return null;
        }

        return url.getBaseUrl();
    }

    public Url getUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl);
    }

    // Переход по короткой ссылке
    public void redirectUrl(Url url) throws IOException {

        if (url == null) {
            System.out.println("Ссылка не найдена.");
            return;
        }

        if (url.getExpiresAt() != null &&
                !url.getExpiresAt().isAfter(Instant.now())) {

            System.out.println("Срок действия ссылки истёк.");
            return;
        }

        if (url.getClickCount() >= url.getClickLimit()) {
            System.out.println("Лимит переходов исчерпан.");
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            System.out.println("Открытие браузера не поддерживается.");
            return;
        }

        Desktop.getDesktop().browse(
                URI.create(url.getBaseUrl())
        );

        url.incrementClickCount();
        url.decrementClickLimit();

        urlPersistenceService.saveUrls();
    }

    public Url findUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl);
    }

    // Удаление ссылки
    public boolean deleteUrl(String shortUrl, UUID ownerId)
            throws IOException {

        Url url = urlRepository.findByShortUrl(shortUrl);

        if (url == null) {
            return false;
        }

        if (!url.getOwnerId().equals(ownerId)) {
            return false;
        }

        urlRepository.delete(shortUrl);

        urlPersistenceService.saveUrls();

        return true;
    }

    // Изменение параметров
    public boolean updateParameters(
            String shortUrl,
            UUID ownerId,
            int newClickLimit,
            Instant newExpiresAt
    ) throws IOException {

        Url url = urlRepository.findByShortUrl(shortUrl);

        if (url == null) {
            return false;
        }

        if (!url.getOwnerId().equals(ownerId)) {
            return false;
        }

        if (newClickLimit <= url.getClickCount()) {
            return false;
        }

        if (newExpiresAt == null ||
                !newExpiresAt.isAfter(Instant.now())) {
            return false;
        }

        url.setClickLimit(newClickLimit);
        url.setExpiresAt(newExpiresAt);

        urlPersistenceService.saveUrls();

        return true;
    }
}
