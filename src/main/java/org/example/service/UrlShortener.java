package org.example.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Random;

public class UrlShortener {

    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                    .toCharArray();

    public static final int urlLength = 6;

    private static final Random random = new Random();

    public static String generateShortUrl() {
        String generatedID = NanoIdUtils.randomNanoId(random, ALPHABET, urlLength);
        return "https://shrt.ru/" + generatedID;
    }
}

