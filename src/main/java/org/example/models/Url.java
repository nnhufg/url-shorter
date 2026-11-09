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
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.shortUrl = shortUrl;
        this.baseUrl = baseUrl;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
        this.expiresAt = expiresAt;
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
        this.shortUrl = shortUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setClickLimit(int clickLimit) {
        this.clickLimit = clickLimit;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    public void decrementClickLimit() {
        this.clickLimit--;
    }
}