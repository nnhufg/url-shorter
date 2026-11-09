package org.example.controller;

import org.example.config.AppConfig;
import org.example.models.Url;
import org.example.models.User;
import org.example.repository.UrlRepository;
import org.example.repository.UserRepository;
import org.example.service.UrlExpirationService;
import org.example.service.UrlPersistenceService;
import org.example.service.UrlShortenerService;
import org.example.service.UserPersistenceService;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleController {

    private final Scanner scanner;
    private final BlockingQueue<String> inputQueue =
            new LinkedBlockingQueue<>();

    private User user;

    private final UrlRepository urlRepository;
    private final UrlShortenerService urlShortenerService;
    private final UrlExpirationService urlExpirationService;

    private final UserPersistenceService userPersistenceService;
    private final UserRepository userRepository;
    private final UrlPersistenceService urlPersistenceService;

    private final AppConfig appConfig;

    public ConsoleController() throws IOException {

        // Конфигурация
        this.appConfig = new AppConfig();

        // Репозиторий пользователей
        this.userRepository = new UserRepository();

        // Сервис сохранения пользователей
        this.userPersistenceService =
                new UserPersistenceService(userRepository);

        // Загружаем пользователей
        userPersistenceService.loadUsers();

        // Репозиторий ссылок
        this.urlRepository = new UrlRepository();

        // Сервис сохранения ссылок
        this.urlPersistenceService =
                new UrlPersistenceService(urlRepository);

        // Загружаем ссылки
        urlPersistenceService.loadUrls();

        // Сервис сокращения ссылок
        this.urlShortenerService =
                new UrlShortenerService(
                        urlRepository,
                        urlPersistenceService
                );

        // Сервис автоматического удаления просроченных ссылок
        this.urlExpirationService = new UrlExpirationService(
                urlRepository,
                urlPersistenceService,
                appConfig.getCleanupIntervalMinutes()
        );

        // Инициализация консольного ввода
        this.scanner = new Scanner(System.in);

        // Отдельный поток чтения консольного ввода
        Thread inputThread = new Thread(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String input = scanner.nextLine();
                    inputQueue.put(input);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;

                } catch (NoSuchElementException e) {

                    break;
                }
            }

        });

        inputThread.setDaemon(true);
        inputThread.start();

        // Запуск автоматической очистки просроченных ссылок
        this.urlExpirationService.start();
    }

    // Главное меню
    public void mainMenu() throws IOException {

        boolean running = true;

        while (running) {

            showMainMenu();

            String userChoice =
                    readMenuInput(this::showMainMenu);

            switch (userChoice) {

                case "1" -> register();

                case "2" -> login();

                case "0" -> {

                    running = false;

                    urlExpirationService.stop();

                    System.out.println(
                            "Завершение работы..."
                    );
                }

                default ->
                        System.out.println(
                                "Неверный выбор! Попробуйте снова!"
                        );
            }
        }
    }

    // Отображение главного меню
    private void showMainMenu() {

        System.out.println(
                "\n===== ВАШ ПЕРСОНАЛЬНЫЙ URL SHORTENER =====\n"
        );

        System.out.println("1. Регистрация");
        System.out.println("2. Вход");
        System.out.println("0. Выход");

        System.out.print("\nВаш выбор: ");
    }

    // Регистрация
    private void register() throws IOException {

        User newUser = new User();

        userRepository.saveUser(newUser);

        userPersistenceService.saveUsers();

        this.user = newUser;

        System.out.println(
                "\n===== РЕГИСТРАЦИЯ УСПЕШНА ====="
        );

        System.out.println(
                "Ваш ID: " + user.getId()
        );

        System.out.println(
                "\nСохраните этот ID. Он понадобится для входа."
        );

        System.out.println(
                "\nТеперь Вам доступен функционал сервиса!"
        );

        run();
    }

    // Вход
    private void login() throws IOException {

        System.out.println("\n===== ВХОД =====");

        System.out.print("Введите ваш ID: ");

        String input = readInput();

        try {

            UUID userId = UUID.fromString(input);

            User foundUser = userRepository.findById(userId);

            if (foundUser == null) {

                System.out.println(
                        "Пользователь с таким ID не найден!"
                );

                return;
            }

            this.user = foundUser;

            System.out.println("\nВход выполнен успешно!");

            System.out.println("Ваш ID: " + user.getId());

            run();

        } catch (IllegalArgumentException e) {

            System.out.println(
                    "Некорректный ID пользователя!"
            );
        }
    }

    // Меню пользователя
    public void run() throws IOException {

        boolean running = true;

        while (running) {

            showUserMenu();

            String choice =
                    readMenuInput(this::showUserMenu);

            switch (choice) {

                case "1" -> createShortUrl();

                case "2" -> browseUrl();

                case "3" -> checkUrls();

                case "4" -> editUrl();

                case "5" -> deleteUrl();

                case "6" -> infoUrl();

                case "0" -> {

                    running = false;

                    this.user = null;

                    System.out.println(
                            "\nВы вышли из аккаунта."
                    );
                }

                default ->
                        System.out.println(
                                "Неверный выбор! Попробуйте снова!"
                        );
            }
        }
    }

    // Отображение меню пользователя
    private void showUserMenu() {

        System.out.println(
                "\n===== МЕНЮ ПОЛЬЗОВАТЕЛЯ ====="
        );

        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Открыть короткую ссылку");
        System.out.println("3. Мои ссылки");
        System.out.println("4. Управление параметрами");
        System.out.println("5. Удалить ссылку");
        System.out.println("6. Информация о ссылке");
        System.out.println("0. Выйти из аккаунта");

        System.out.print("\nВаш выбор: ");
    }



    public void createShortUrl() throws IOException {

        System.out.print("\nВведите ссылку для сокращения: ");

        String baseUrl = readInput();

        int clickLimit = appConfig.getDefaultClickLimit();

        Instant expiresAt = Instant.now().plus(
                appConfig.getDefaultTtlDays(),
                ChronoUnit.DAYS
        );

        try {
            String shortUrl = urlShortenerService.shorten(
                    user.getId(),
                    baseUrl,
                    clickLimit,
                    expiresAt
            );

            System.out.println("\nКороткая ссылка успешно создана!");
            System.out.println("Ваша ссылка: " + shortUrl);
            System.out.println("Лимит переходов: " + clickLimit);
            System.out.println("Срок действия до: " + expiresAt);

        } catch (IllegalArgumentException e) {
            System.out.println("\nОшибка: " + e.getMessage());

        } catch (IOException e) {
            System.out.println(
                    "\nОшибка сохранения ссылки: " + e.getMessage()
            );
        }
    }


    /**
     * Открытие короткой ссылки.
     */
    public void browseUrl() throws IOException {

        System.out.print(
                "\nВведите короткую ссылку для перехода: "
        );

        String shortUrl = readInput();

        Url url =
                urlRepository.findByShortUrl(shortUrl);

        if (url == null) {

            System.out.println(
                    "Короткая ссылка не найдена!"
            );

            return;
        }

        urlShortenerService.redirectUrl(url);

        System.out.println(
                "Количество переходов: "
                        + url.getClickCount()
        );

        System.out.println(
                "Лимит переходов: "
                        + url.getClickLimit()
        );

        System.out.println(
                "Срок действия ссылки: "
                        + url.getExpiresAt()
        );
    }

    /**
     * Показать ссылки текущего пользователя.
     */
    public void checkUrls() {

        System.out.println(
                "\n===== ВАШИ СОЗДАННЫЕ ССЫЛКИ ====="
        );

        List<Url> urls =
                urlRepository.findAllByOwnerId(
                        user.getId()
                );

        if (urls.isEmpty()) {

            System.out.println(
                    "У вас пока нет созданных ссылок."
            );

            return;
        }

        for (Url url : urls) {

            System.out.println(
                    "\nКороткая ссылка: "
                            + url.getShortUrl()
            );

            System.out.println(
                    "Оригинальная ссылка: "
                            + url.getBaseUrl()
            );

            System.out.println(
                    "Переходов: "
                            + url.getClickCount()
                            + " / "
                            + url.getClickLimit()
            );

            System.out.println(
                    "Действует до: "
                            + url.getExpiresAt()
            );
        }
    }

    /**
     * Изменение параметров ссылки.
     */
    public void editUrl() throws IOException {

        System.out.print(
                "\nВведите короткую ссылку: "
        );

        String shortUrl = readInput();

        System.out.print(
                "Введите новый лимит переходов: "
        );

        int newClickLimit;

        try {

            newClickLimit =
                    Integer.parseInt(readInput());

        } catch (NumberFormatException e) {

            System.out.println(
                    "Лимит должен быть целым числом!"
            );

            return;
        }

        if (newClickLimit <= 0) {

            System.out.println(
                    "Лимит должен быть больше нуля!"
            );

            return;
        }

        System.out.print(
                "Введите новый срок действия в днях: "
        );

        int days;

        try {

            days = Integer.parseInt(readInput());

        } catch (NumberFormatException e) {

            System.out.println(
                    "Количество дней должно быть целым числом!"
            );

            return;
        }

        if (days <= 0) {

            System.out.println(
                    "Количество дней должно быть больше нуля!"
            );

            return;
        }

        Instant newExpiresAt =
                Instant.now().plus(
                        days,
                        ChronoUnit.DAYS
                );

        boolean updated =
                urlShortenerService.updateParameters(
                        shortUrl,
                        user.getId(),
                        newClickLimit,
                        newExpiresAt
                );

        if (updated) {

            System.out.println(
                    "\nПараметры ссылки успешно изменены!"
            );

        } else {

            System.out.println(
                    "\nНе удалось изменить параметры."
            );

            System.out.println(
                    "Проверьте существование ссылки, "
                            + "права владельца и введённые параметры."
            );
        }
    }

    /**
     * Удаление ссылки.
     */
    public void deleteUrl() throws IOException {

        System.out.print(
                "\nВведите короткую ссылку для удаления: "
        );

        String shortUrl = readInput();

        boolean deleted =
                urlShortenerService.deleteUrl(
                        shortUrl,
                        user.getId()
                );

        if (deleted) {

            System.out.println(
                    "Ссылка успешно удалена!"
            );

        } else {

            System.out.println(
                    "Ссылка не найдена или у вас нет прав "
                            + "на её удаление!"
            );
        }
    }

    /**
     * Информация о ссылке.
     */
    public void infoUrl() throws IOException {

        System.out.print(
                "\nВведите короткую ссылку: "
        );

        String shortUrl = readInput();

        Url url =
                urlRepository.findByShortUrl(shortUrl);

        if (url == null ||
                !url.getOwnerId().equals(user.getId())) {

            System.out.println(
                    "Ссылка не найдена или у вас нет прав на просмотр!"
            );

            return;
        }

        System.out.println(
                "\n===== ИНФОРМАЦИЯ О ССЫЛКЕ ====="
        );

        System.out.println(
                "ID: " + url.getId()
        );

        System.out.println(
                "Владелец: " + url.getOwnerId()
        );

        System.out.println(
                "Короткая ссылка: " + url.getShortUrl()
        );

        System.out.println(
                "Оригинальная ссылка: " + url.getBaseUrl()
        );

        System.out.println(
                "Переходов: "
                        + url.getClickCount()
                        + " / "
                        + url.getClickLimit()
        );

        System.out.println(
                "Действует до: " + url.getExpiresAt()
        );
    }

    /**
     * Чтение пользовательского ввода.
     * Одновременно проверяет очередь уведомлений.
     */
    private String readInput() throws IOException {

        try {

            while (true) {

                String notification =
                        urlExpirationService.getNotification();

                if (notification != null) {

                    System.out.println(
                            "\n" + notification
                    );
                }

                String input = inputQueue.poll(
                        200,
                        TimeUnit.MILLISECONDS
                );

                if (input != null) {
                    return input;
                }
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IOException(
                    "Ожидание ввода было прервано",
                    e
            );
        }
    }

    /**
     * Чтение ввода в меню.
     * При получении уведомления меню отображается повторно.
     */
    private String readMenuInput(
            Runnable showMenu
    ) throws IOException {

        try {

            while (true) {

                String notification;
                boolean hasNotification = false;

                while ((notification =
                        urlExpirationService.getNotification()) != null) {

                    System.out.println(
                            "\n" + notification
                    );

                    hasNotification = true;
                }

                if (hasNotification) {
                    showMenu.run();
                }

                String input = inputQueue.poll(
                        20,
                        TimeUnit.MILLISECONDS
                );

                if (input != null) {
                    return input;
                }
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IOException(
                    "Ожидание ввода было прервано",
                    e
            );
        }
    }
}
