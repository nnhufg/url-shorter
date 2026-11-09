
package org.example.repository;

import org.example.models.Url;

import java.util.*;
import java.time.Instant;

public class UrlRepository {

    private final Map<String, Url> urls = new HashMap<>();

    public synchronized void save(Url url) {
        urls.put(url.getShortUrl(), url);
    }

    public synchronized Url findByShortUrl(String shortUrl) {
        return urls.get(shortUrl);
    }

    public synchronized boolean ifExists(String shortUrl) {
        return urls.containsKey(shortUrl);
    }

    public synchronized void delete(String shortUrl) {
        urls.remove(shortUrl);
    }

    public synchronized void showMap() {

        for (Map.Entry<String, Url> entry : urls.entrySet()) {

            System.out.println(
                    entry.getKey() + " : " + entry.getValue().getBaseUrl()
            );
        }
    }

    public synchronized boolean deleteExpiredUrls() {

        Instant now = Instant.now();
        boolean deleted = false;

        Iterator<Map.Entry<String, Url>> iterator =
                urls.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<String, Url> entry = iterator.next();

            if (entry.getValue().getExpiresAt() != null &&
                    !entry.getValue().getExpiresAt().isAfter(now)) {

                iterator.remove();
                deleted = true;
            }
        }

        return deleted;
    }

    public synchronized List<Url> findAll() {
        return new ArrayList<>(urls.values());
    }

    public synchronized List<Url> findAllByOwnerId(UUID ownerId) {

        return urls.values().stream()
                .filter(url -> Objects.equals(url.getOwnerId(), ownerId))
                .toList();
    }
}
