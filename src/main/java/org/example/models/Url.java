
package org.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;
import java.time.Instant;

public class Url {

    private final UUID id;
    private final UUID ownerId;

    private String shortUrl;
    private String baseUrl;

    private int clickLimit;
    private int clickCount;

    private Instant expiresAt;

    public Url(
            UUID ownerId,
            String shortUrl,
            String baseUrl,
            int clickLimit,
            Instant expiresAt
    ) {
        this(
                UUID.randomUUID(),
                ownerId,
                shortUrl,
                baseUrl,
                clickLimit,
                0,
                expiresAt
        );
    }

    @JsonCreator
    public Url(
            @JsonProperty("id") UUID id,
            @JsonProperty("ownerId") UUID ownerId,
            @JsonProperty("shortUrl") String shortUrl,
            @JsonProperty("baseUrl") String baseUrl,
            @JsonProperty("clickLimit") int clickLimit,
            @JsonProperty("clickCount") int clickCount,
            @JsonProperty("expiresAt") Instant expiresAt
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "ID ссылки не может быть null."
            );
        }

        if (ownerId == null) {
            throw new IllegalArgumentException(
                    "ID владельца не может быть null."
            );
        }

        if (shortUrl == null || shortUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Короткая ссылка не может быть пустой."
            );
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Исходный URL не может быть пустым."
            );
        }

        if (clickLimit <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть больше нуля."
            );
        }

        if (clickCount < 0) {
            throw new IllegalArgumentException(
                    "Количество переходов не может быть отрицательным."
            );
        }

        this.id = id;
        this.ownerId = ownerId;
        this.shortUrl = shortUrl;
        this.baseUrl = baseUrl;
        this.clickLimit = clickLimit;
        this.clickCount = clickCount;
        this.expiresAt = expiresAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public int getClickCount() {
        return clickCount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setShortUrl(String shortUrl) {
        if (shortUrl == null || shortUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Короткая ссылка не может быть пустой."
            );
        }

        this.shortUrl = shortUrl;
    }

    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Исходный URL не может быть пустым."
            );
        }

        this.baseUrl = baseUrl;
    }

    public void setClickLimit(int clickLimit) {
        if (clickLimit <= 0) {
            throw new IllegalArgumentException(
                    "Лимит переходов должен быть больше нуля."
            );
        }

        this.clickLimit = clickLimit;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void incrementClickCount() {
        if (clickCount >= clickLimit) {
            throw new IllegalStateException(
                    "Лимит переходов исчерпан."
            );
        }

        clickCount++;
    }
}
