
package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.example.models.Url;
import org.example.repository.UrlRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.List;

public class UrlPersistenceService {

    private final UrlRepository urlRepository;
    private final ObjectMapper mapper;
    private final File file;

    public UrlPersistenceService(UrlRepository urlRepository) {

        if (urlRepository == null) {
            throw new IllegalArgumentException(
                    "UrlRepository не может быть null."
            );
        }

        this.urlRepository = urlRepository;

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.file = new File("data/urls.json");
    }

    // Сохранение всех ссылок
    public void saveUrls() throws IOException {

        File parentDirectory = file.getAbsoluteFile().getParentFile();

        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory.toPath());
        }

        List<Url> allUrls = urlRepository.findAll();

        Path temporaryFile = Files.createTempFile(
                parentDirectory.toPath(),
                "urls-",
                ".tmp"
        );

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporaryFile.toFile(), allUrls);

            try {
                Files.move(
                        temporaryFile,
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        temporaryFile,
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    // Загрузка ссылок
    public void loadUrls() throws IOException {

        if (!file.exists()) {
            saveUrls();
            return;
        }

        List<Url> allUrls;

        try {
            allUrls = mapper.readValue(
                    file,
                    new TypeReference<List<Url>>() {}
            );

        } catch (JsonProcessingException e) {
            throw new IOException(
                    "Файл urls.json повреждён или содержит некорректные данные.",
                    e
            );
        }

        if (allUrls == null) {
            throw new IOException(
                    "Файл urls.json содержит некорректные данные."
            );
        }

        for (Url url : allUrls) {

            if (url == null) {
                throw new IOException(
                        "Файл urls.json содержит пустую запись."
                );
            }
        }

        for (Url url : allUrls) {
            urlRepository.save(url);
        }
    }
}
