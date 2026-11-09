
package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.example.models.User;
import org.example.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.List;

public class UserPersistenceService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final File file;

    public UserPersistenceService(UserRepository userRepository) {

        if (userRepository == null) {
            throw new IllegalArgumentException(
                    "UserRepository не может быть null."
            );
        }

        this.userRepository = userRepository;

        this.mapper = new ObjectMapper();
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.file = new File("data/users.json");
    }

    public void saveUsers() throws IOException {

        File parentDirectory = file.getAbsoluteFile().getParentFile();

        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory.toPath());
        }

        List<User> allUsers = userRepository.findAll();

        Path temporaryFile = Files.createTempFile(
                parentDirectory.toPath(),
                "users-",
                ".tmp"
        );

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temporaryFile.toFile(), allUsers);

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

    public void loadUsers() throws IOException {

        if (!file.exists()) {
            saveUsers();
            return;
        }

        List<User> allUsers;

        try {
            allUsers = mapper.readValue(
                    file,
                    new TypeReference<List<User>>() {}
            );

        } catch (JsonProcessingException e) {
            throw new IOException(
                    "Файл users.json повреждён или содержит некорректные данные.",
                    e
            );
        }

        if (allUsers == null) {
            throw new IOException(
                    "Файл users.json содержит некорректные данные."
            );
        }

        for (User user : allUsers) {

            if (user == null) {
                throw new IOException(
                        "Файл users.json содержит пустую запись."
                );
            }
        }

        for (User user : allUsers) {
            userRepository.saveUser(user);
        }
    }
}
