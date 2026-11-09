package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.example.models.Url;
import org.example.repository.UrlRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UrlPersistenceService {

    private final UrlRepository urlRepository;
    private final ObjectMapper mapper;
    private final File file;

    public UrlPersistenceService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.file = new File("data/urls.json");
    }

    // Сохранение всех ссылок
    public void saveUrls() throws IOException {

        file.getParentFile().mkdirs();

        List<Url> allUrls = urlRepository.findAll();

        mapper.writeValue(file, allUrls);
    }

    // Загрузка ссылок
    public void loadUrls() throws IOException {

        if (!file.exists()) {
            return;
        }

        List<Url> allUrls = mapper.readValue(
                file,
                new TypeReference<List<Url>>() {}
        );

        for (Url url : allUrls) {
            urlRepository.save(url);
        }
    }
}