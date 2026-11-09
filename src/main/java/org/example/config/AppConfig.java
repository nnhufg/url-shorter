package org.example.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {

    private final Properties properties;

    public AppConfig() throws IOException {

        this.properties = new Properties();

        try (FileInputStream input =
                     new FileInputStream("config.properties")) {

            properties.load(input);
        }
    }

    private int getPositiveInt(String key) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Отсутствует настройка: " + key
            );
        }

        int number;

        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Настройка должна быть целым числом: " + key
            );
        }

        if (number <= 0) {
            throw new IllegalArgumentException(
                    "Настройка должна быть больше нуля: " + key
            );
        }

        return number;
    }

    public int getDefaultTtlDays() {
        return getPositiveInt("url.default-ttl-days");
    }

    public int getDefaultClickLimit() {
        return getPositiveInt("url.default-click-limit");
    }

    public int getCleanupIntervalMinutes() {
        return getPositiveInt("url.cleanup-interval-minutes");
    }
}
