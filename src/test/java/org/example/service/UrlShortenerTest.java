
package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerTest {

    @Test
    void generateShortUrlShouldReturnCorrectDomain() {
        String result = UrlShortener.generateShortUrl();

        assertTrue(result.startsWith("https://shrt.ru/"));
    }

    @Test
    void generateShortUrlShouldReturnCorrectLength() {
        String result = UrlShortener.generateShortUrl();

        String shortId = result.substring(
                "https://shrt.ru/".length()
        );

        assertEquals(6, shortId.length());
    }

    @Test
    void generateShortUrlShouldContainOnlyAllowedCharacters() {
        String result = UrlShortener.generateShortUrl();

        String shortId = result.substring(
                "https://shrt.ru/".length()
        );

        assertTrue(shortId.matches("[A-Za-z0-9]{6}"));
    }

    @Test
    void generateShortUrlShouldGenerateDifferentIds() {
        String first = UrlShortener.generateShortUrl();
        String second = UrlShortener.generateShortUrl();

        assertNotEquals(first, second);
    }
}
