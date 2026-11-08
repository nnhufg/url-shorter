package org.example.controller;

import org.example.models.Url;
import org.example.models.User;
import org.example.repository.UrlRepository;
import org.example.service.UrlExpirationService;
import org.example.service.UrlShortenerService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;


public class ConsoleController {

    private final Scanner scanner;
    private final User user;
    private final UrlRepository urlRepository;
    private final UrlShortenerService urlShortenerService;
    private final UrlExpirationService urlExpirationService;

    public ConsoleController() {
        this.scanner = new Scanner(System.in);

        this.user = new User();

        this.urlRepository = new UrlRepository();

        this.urlShortenerService =
                new UrlShortenerService(urlRepository);

        this.urlExpirationService = new UrlExpirationService(urlRepository);
        this.urlExpirationService.start();
    }

    public void run() throws IOException {

        boolean running = true;

        while (running) {

            System.out.println(
                    "\n===== ВАШ ПЕРСОНАЛЬНЫЙ URL SHORTENER ====="
            );

            System.out.println("Ваш ID: " + user.getId());
            System.out.println();

            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Открыть короткую ссылку");
            System.out.println("3. Мои ссылки");
            System.out.println("4. Управление параметрами");
            System.out.println("5. Удалить ссылку");
            System.out.println("6. Информация о ссылке");
            System.out.println("0. Выход");

            System.out.print("\nВаш выбор: ");

            String choice = scanner.nextLine();

            switch (choice) {

                case "1" -> createShortUrl();

                case "2" -> browseUrl();

                case "3" -> checkUrls();

                case "4" -> editUrl();

                case "5" -> deleteUrl();

                case "0" -> {
                    running = false;
                    urlExpirationService.stop();
                    System.out.println("Завершение работы...");
                }

                default -> System.out.println(
                        "Неверный выбор. Выберите пункт из меню!"
                );
            }
        }
    }

    // 1. Создание короткой ссылки
    public void createShortUrl() {

        System.out.print(
                "\nВведите ссылку для сокращения: "
        );

        String baseUrl = scanner.nextLine();

        Instant expiresAt =
                Instant.now().plus(Duration.ofDays(7));

        int clickLimit = 5;

        String shortUrl = urlShortenerService.shorten(
                user.getId(),
                baseUrl,
                clickLimit,
                expiresAt
        );

        System.out.println("\nКороткая ссылка успешно создана!");
        System.out.println("Ваша ссылка: " + shortUrl);
        System.out.println("Лимит переходов: " + clickLimit);
    }

    // 2. Открытие короткой ссылки
    public void browseUrl() throws IOException {

        System.out.print(
                "\nВведите короткую ссылку для перехода: "
        );

        String shortUrl = scanner.nextLine();

        Url url = urlRepository.findByShortUrl(shortUrl);

        if (url == null) {
            System.out.println("Короткая ссылка не найдена!");
            return;
        }

        urlShortenerService.redirectUrl(url);

        System.out.println(
                "Количество переходов: " + url.getClickCount()
        );
    }

    // 3. Просмотр ссылок
    public void checkUrls() {

        System.out.println(
                "\n===== ВАШИ СОЗДАННЫЕ ССЫЛКИ ====="
        );

        urlRepository.showMap();
    }

    // 4. Управление параметрами
    public void editUrl() {

        System.out.println("\n===== УПРАВЛЕНИЕ ПАРАМЕТРАМИ =====");

        System.out.print("Введите короткую ссылку: ");
        String shortUrl = scanner.nextLine();

        System.out.print("Введите новый лимит кликов: ");

        int newClickLimit;

        try {
            newClickLimit = Integer.parseInt(scanner.nextLine());

            if (newClickLimit <= 0) {
                System.out.println("Лимит должен быть положительным числом!");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка! Необходимо ввести целое число.");
            return;
        }

        System.out.print("Введите новый срок действия в днях: ");

        int days;

        try {
            days = Integer.parseInt(scanner.nextLine());

            if (days <= 0) {
                System.out.println("Срок действия должен быть больше нуля!");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка! Необходимо ввести целое число.");
            return;
        }

        Instant newExpiresAt = Instant.now().plus(Duration.ofDays(days));

        boolean updated = urlShortenerService.updateParameters(
                shortUrl,
                user.getId(),
                newClickLimit,
                newExpiresAt
        );

        if (updated) {
            System.out.println("Параметры ссылки успешно обновлены!");
            System.out.println("Новый лимит: " + newClickLimit);
            System.out.println("Новый срок действия: " + days + " дней");
        } else {
            System.out.println(
                    "Не удалось изменить параметры. " +
                            "Проверьте ссылку, права доступа и новые значения."
            );
        }
    }

    // 5. Удаление ссылки
    public void deleteUrl() {

        System.out.println("\nВведите короткую ссылку для удаления:");
        String shortUrl = scanner.nextLine();

        boolean deleted = urlShortenerService.deleteUrl(
                shortUrl,
                user.getId()
        );

        if (deleted) {
            System.out.println("Ссылка успешно удалена!");
        } else {
            System.out.println("Ссылка не найдена или у вас нет прав на её удаление!");
        }
    }

}
