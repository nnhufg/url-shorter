
package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.Url;
import org.example.repository.UrlRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlPersistenceServiceTest {

    @TempDir
    Path tempDir;

    private UrlRepository urlRepository;
    private UrlPersistenceService persistenceService;
    private File jsonFile;

    @BeforeEach
    void setUp() throws Exception {

        urlRepository = mock(UrlRepository.class);

        persistenceService =
                new UrlPersistenceService(urlRepository);

        // Подменяем стандартный путь временной директорией
        Field fileField =
                UrlPersistenceService.class.getDeclaredField("file");

        fileField.setAccessible(true);

        jsonFile = tempDir.resolve("urls.json").toFile();

        fileField.set(persistenceService, jsonFile);
    }

    @Test
    void constructorShouldRejectNullRepository() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new UrlPersistenceService(null)
        );
    }

    @Test
    void saveUrlsShouldCreateJsonFile() throws IOException {

        when(urlRepository.findAll())
                .thenReturn(List.of());

        persistenceService.saveUrls();

        assertTrue(jsonFile.exists());
    }

    @Test
    void saveUrlsShouldSaveRepositoryData() throws Exception {

        Url url = new Url(
                UUID.randomUUID(),
                "https://example.com",
                "https://shrt.ru/abc123",
                10,
                Instant.now()
        );

        when(urlRepository.findAll())
                .thenReturn(List.of(url));

        persistenceService.saveUrls();

        ObjectMapper mapper = new ObjectMapper();

        List<?> savedUrls = mapper.readValue(
                jsonFile,
                List.class
        );

        assertEquals(1, savedUrls.size());
    }

    @Test
    void saveUrlsShouldCreateParentDirectory() throws Exception {

        File nestedFile = tempDir
                .resolve("nested")
                .resolve("data")
                .resolve("urls.json")
                .toFile();

        Field fileField =
                UrlPersistenceService.class.getDeclaredField("file");

        fileField.setAccessible(true);
        fileField.set(persistenceService, nestedFile);

        when(urlRepository.findAll())
                .thenReturn(List.of());

        persistenceService.saveUrls();

        assertTrue(nestedFile.exists());
    }

    @Test
    void loadUrlsShouldCreateFileWhenItDoesNotExist()
            throws IOException {

        persistenceService.loadUrls();

        assertTrue(jsonFile.exists());

        verify(urlRepository).findAll();
    }

    @Test
    void loadUrlsShouldRejectInvalidJson() throws IOException {

        java.nio.file.Files.writeString(
                jsonFile.toPath(),
                "{ invalid json"
        );

        assertThrows(
                IOException.class,
                () -> persistenceService.loadUrls()
        );
    }

    @Test
    void loadUrlsShouldRejectNullJson() throws IOException {

        java.nio.file.Files.writeString(
                jsonFile.toPath(),
                "null"
        );

        assertThrows(
                IOException.class,
                () -> persistenceService.loadUrls()
        );
    }

    @Test
    void loadUrlsShouldRejectNullEntries() throws IOException {

        java.nio.file.Files.writeString(
                jsonFile.toPath(),
                "[null]"
        );

        assertThrows(
                IOException.class,
                () -> persistenceService.loadUrls()
        );
    }

    @Test
    void loadUrlsShouldNotSaveAnythingWhenJsonIsEmpty()
            throws IOException {

        java.nio.file.Files.writeString(
                jsonFile.toPath(),
                "[]"
        );

        persistenceService.loadUrls();

        verify(urlRepository, never())
                .save(any(Url.class));
    }
}
