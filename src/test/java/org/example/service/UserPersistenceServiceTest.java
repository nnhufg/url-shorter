package org.example.service;

import org.example.models.User;
import org.example.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPersistenceServiceTest {

    @TempDir
    Path tempDir;

    private UserRepository userRepository;
    private UserPersistenceService persistenceService;
    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {

        userRepository = new UserRepository();

        testFile = tempDir.resolve("users.json");

        persistenceService = createServiceWithFile(
                userRepository,
                testFile
        );
    }

    // Создание сервиса с тестовым расположением файла
    private UserPersistenceService createServiceWithFile(
            UserRepository repository,
            Path filePath
    ) throws Exception {

        UserPersistenceService service =
                new UserPersistenceService(repository);

        Field fileField =
                UserPersistenceService.class.getDeclaredField("file");

        fileField.setAccessible(true);
        fileField.set(service, filePath.toFile());

        return service;
    }

    // 1. Проверка конструктора с null
    @Test
    void shouldThrowExceptionWhenRepositoryIsNull() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new UserPersistenceService(null)
        );
    }

    // 2. Проверка сохранения пользователей
    @Test
    void shouldSaveUsersToFile() throws IOException {

        User user1 = new User();
        User user2 = new User();

        userRepository.saveUser(user1);
        userRepository.saveUser(user2);

        persistenceService.saveUsers();

        assertTrue(Files.exists(testFile));

        String content = Files.readString(testFile);

        assertTrue(content.contains(user1.getId().toString()));
        assertTrue(content.contains(user2.getId().toString()));
    }

    // 3. Проверка создания папок при сохранении
    @Test
    void shouldCreateDirectoriesWhenSavingUsers() throws Exception {

        Path nestedFile = tempDir
                .resolve("nested")
                .resolve("data")
                .resolve("users.json");

        UserPersistenceService service =
                createServiceWithFile(userRepository, nestedFile);

        service.saveUsers();

        assertTrue(Files.exists(nestedFile));
    }

    // 4. Проверка загрузки пользователей
    @Test
    void shouldLoadUsersFromFile() throws IOException {

        User user1 = new User();
        User user2 = new User();

        String json = """
                [
                    {"id": "%s"},
                    {"id": "%s"}
                ]
                """.formatted(
                user1.getId(),
                user2.getId()
        );

        Files.writeString(testFile, json);

        persistenceService.loadUsers();

        assertEquals(2, userRepository.findAll().size());

        assertNotNull(userRepository.findById(user1.getId()));
        assertNotNull(userRepository.findById(user2.getId()));
    }

    // 5. Проверка загрузки пустого списка
    @Test
    void shouldLoadEmptyList() throws IOException {

        Files.writeString(testFile, "[]");

        persistenceService.loadUsers();

        assertTrue(userRepository.findAll().isEmpty());
    }

    // 6. Проверка отсутствующего файла
    @Test
    void shouldCreateFileWhenItDoesNotExist() throws IOException {

        User user = new User();
        userRepository.saveUser(user);

        persistenceService.loadUsers();

        assertTrue(Files.exists(testFile));

        String content = Files.readString(testFile);

        assertTrue(content.contains(user.getId().toString()));
    }

    // 7. Проверка повреждённого JSON
    @Test
    void shouldThrowExceptionWhenJsonIsInvalid() throws IOException {

        Files.writeString(testFile, "{ invalid json");

        assertThrows(
                IOException.class,
                () -> persistenceService.loadUsers()
        );
    }

    // 8. Проверка JSON со значением null
    @Test
    void shouldThrowExceptionWhenJsonContainsNull() throws IOException {

        Files.writeString(testFile, "null");

        IOException exception = assertThrows(
                IOException.class,
                () -> persistenceService.loadUsers()
        );

        assertTrue(exception.getMessage().contains("некорректные данные"));
    }

    // 9. Проверка пустой записи внутри JSON
    @Test
    void shouldThrowExceptionWhenJsonContainsNullUser() throws IOException {

        Files.writeString(testFile, "[null]");

        IOException exception = assertThrows(
                IOException.class,
                () -> persistenceService.loadUsers()
        );

        assertTrue(exception.getMessage().contains("пустую запись"));
    }

    // 10. Проверка сохранения нескольких пользователей
    @Test
    void shouldSaveAllUsers() throws IOException, NoSuchFieldException, IllegalAccessException {

        User user1 = new User();
        User user2 = new User();
        User user3 = new User();

        userRepository.saveUser(user1);
        userRepository.saveUser(user2);
        userRepository.saveUser(user3);

        persistenceService.saveUsers();

        UserRepository secondRepository = new UserRepository();

        UserPersistenceService secondService =
                new UserPersistenceService(secondRepository);

        Field fileField =
                UserPersistenceService.class.getDeclaredField("file");

        fileField.setAccessible(true);
        fileField.set(secondService, testFile.toFile());

        secondService.loadUsers();

        assertEquals(3, secondRepository.findAll().size());

        assertNotNull(secondRepository.findById(user1.getId()));
        assertNotNull(secondRepository.findById(user2.getId()));
        assertNotNull(secondRepository.findById(user3.getId()));
    }
}
