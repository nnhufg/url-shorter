package org.example.integration;

import org.example.models.User;
import org.example.repository.UserRepository;
import org.example.service.UserPersistenceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserPersistenceIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndRestoreUsers() throws Exception {

        // Создаём исходный репозиторий
        UserRepository firstRepository = new UserRepository();

        User user1 = new User();
        User user2 = new User();

        firstRepository.saveUser(user1);
        firstRepository.saveUser(user2);

        // Создаём сервис сохранения
        UserPersistenceService saveService =
                new UserPersistenceService(firstRepository);

        Path filePath = tempDir.resolve("users.json");

        setFilePath(saveService, filePath);

        // Сохраняем пользователей
        saveService.saveUsers();

        assertTrue(filePath.toFile().exists());

        // Создаём новый репозиторий — имитируем перезапуск приложения
        UserRepository secondRepository = new UserRepository();

        UserPersistenceService loadService =
                new UserPersistenceService(secondRepository);

        setFilePath(loadService, filePath);

        // Загружаем пользователей
        loadService.loadUsers();

        // Проверяем восстановленные данные
        assertEquals(2, secondRepository.findAll().size());

        assertNotNull(secondRepository.findById(user1.getId()));
        assertNotNull(secondRepository.findById(user2.getId()));
    }

    private void setFilePath(
            UserPersistenceService service,
            Path path
    ) throws Exception {

        Field fileField =
                UserPersistenceService.class.getDeclaredField("file");

        fileField.setAccessible(true);
        fileField.set(service, path.toFile());
    }
}
