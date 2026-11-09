package org.example.repository;

import org.example.models.Url;

import java.util.*;
import java.time.Instant;

public class UrlRepository {

    private final Map<String, Url> urls = new HashMap<>();

    public void save(Url url) {
        urls.put(url.getShortUrl(), url);
    }

    public Url findByShortUrl(String shortUrl) {
        return urls.get(shortUrl);
    }

    public boolean ifExists(String shortUrl) {
        return urls.containsKey(shortUrl);
    }

    public void delete(String shortUrl) {
        urls.remove(shortUrl);
    }

    public void showMap() {
        for (Map.Entry<String, Url> entry : urls.entrySet()) {
            System.out.println(
                    entry.getKey() + " : " + entry.getValue().getBaseUrl()
            );
        }
    }

    public void deleteExpiredUrls() {
        Instant now = Instant.now();

        Iterator<Map.Entry<String, Url>> iterator = urls.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Url> entry = iterator.next();

            if (entry.getValue().getExpiresAt() != null &&
                    entry.getValue().getExpiresAt().isBefore(now)) {

                iterator.remove();
            }
        }
    }

    public List<Url> findAll() {
        return new ArrayList<>(urls.values());
    }

    public List<Url> findAllByOwnerId(UUID ownerId) {
        return urls.values().stream()
                .filter(url -> url.getOwnerId().equals(ownerId))
                .toList();
    }
}