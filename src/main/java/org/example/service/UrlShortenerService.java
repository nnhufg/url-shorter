
package org.example.service;

import org.example.models.Url;
import org.example.repository.UrlRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

public class UrlShortenerService {

    private final UrlRepository urlRepository;
    private final UrlPersistenceService urlPersistenceService;

    public UrlShortenerService(
            UrlRepository urlRepository,
            UrlPersistenceService urlPersistenceService
    ) {
        if (urlRepository == null || urlPersistenceService == null) {
            throw new IllegalArgumentException(
                    "Репозиторий и сервис сохранения не могут быть null."
            );
        }

        this.urlRepository = urlRepository;
        this.urlPersistenceService = urlPersistenceService;
    }

    // Проверка URL
    private void validateUrl(String baseUrl) {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "URL не может быть пустым."
            );
        }

        try {
            URI uri = new URI(baseUrl.trim());

            String scheme = uri.getScheme();

            if (scheme == null ||
                    !(scheme.equalsIgnoreCase("http") ||
                            scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException(
                        "Поддерживаются только протоколы HTTP и HTTPS."
                );
            }

            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(
                        "Некорректный адрес сайта."
                );
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Некорректный формат URL.", e
            );
        }
    }

    // Создание короткой ссылки
    public String shorten(
            UUID ownerId,
            String baseUrl,
            int clickLimit,
            Instant expiresAt
    ) throws IOException {

        if (ownerId == null) {
            throw new IllegalArgumentException(
                    "ID пользователя не может быть null."
            );
        }

        validateUrl(baseUrl);

        if (clickLimit <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть больше нуля."
            );
        }

        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "Срок действия ссылки должен быть в будущем."
            );
        }

        String shortUrl;

        do {
            shortUrl = UrlShortener.generateShortUrl();
        } while (urlRepository.ifExists(shortUrl));

        Url url = new Url(
                ownerId,
                shortUrl,
                baseUrl.trim(),
                clickLimit,
                expiresAt
        );

        urlRepository.save(url);

        try {
            urlPersistenceService.saveUrls();
        } catch (IOException e) {
            urlRepository.delete(shortUrl);
            throw e;
        }

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

        try {
            Desktop.getDesktop().browse(
                    URI.create(url.getBaseUrl())
            );
        } catch (IllegalArgumentException e) {
            throw new IOException(
                    "Не удалось открыть ссылку: некорректный URL.", e
            );
        } catch (UnsupportedOperationException e) {
            throw new IOException(
                    "Операция открытия браузера не поддерживается.", e
            );
        }

        url.incrementClickCount();

        urlPersistenceService.saveUrls();
    }

    public Url findUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl);
    }

    // Удаление ссылки
    public boolean deleteUrl(String shortUrl, UUID ownerId)
            throws IOException {

        if (shortUrl == null || shortUrl.isBlank() || ownerId == null) {
            throw new IllegalArgumentException(
                    "Короткая ссылка и ID пользователя обязательны."
            );
        }

        Url url = urlRepository.findByShortUrl(shortUrl);

        if (url == null) {
            return false;
        }

        if (!ownerId.equals(url.getOwnerId())) {
            return false;
        }

        urlRepository.delete(shortUrl);

        try {
            urlPersistenceService.saveUrls();
        } catch (IOException e) {
            urlRepository.save(url);
            throw e;
        }

        return true;
    }

    // Изменение параметров
    public boolean updateParameters(
            String shortUrl,
            UUID ownerId,
            int newClickLimit,
            Instant newExpiresAt
    ) throws IOException {

        if (shortUrl == null || shortUrl.isBlank() || ownerId == null) {
            throw new IllegalArgumentException(
                    "Короткая ссылка и ID пользователя обязательны."
            );
        }

        if (newClickLimit <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть больше нуля."
            );
        }

        if (newExpiresAt == null ||
                !newExpiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException(
                    "Новая дата истечения должна быть в будущем."
            );
        }

        Url url = urlRepository.findByShortUrl(shortUrl);

        if (url == null) {
            return false;
        }

        if (!ownerId.equals(url.getOwnerId())) {
            return false;
        }

        if (newClickLimit <= url.getClickCount()) {
            throw new IllegalArgumentException(
                    "Новый лимит должен быть больше уже использованного количества переходов."
            );
        }

        int oldClickLimit = url.getClickLimit();
        Instant oldExpiresAt = url.getExpiresAt();

        url.setClickLimit(newClickLimit);
        url.setExpiresAt(newExpiresAt);

        try {
            urlPersistenceService.saveUrls();
        } catch (IOException e) {
            url.setClickLimit(oldClickLimit);
            url.setExpiresAt(oldExpiresAt);
            throw e;
        }

        return true;
    }
}
