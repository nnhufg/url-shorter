package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.User;
import org.example.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UserPersistenceService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final File file;

    public UserPersistenceService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.mapper = new ObjectMapper();
        this.file = new File("data/users.json");
    }

    public void saveUsers() throws IOException {
        file.getParentFile().mkdirs();

        List<User> allUsers = userRepository.findAll();

        mapper.writeValue(file, allUsers);
    }

    public void loadUsers() throws IOException {

        if (!file.exists()) {
            return;
        }

        List<User> allUsers = mapper.readValue(
                file,
                new TypeReference<List<User>>() {}
        );

        for (User user : allUsers) {
            userRepository.saveUser(user);
        }
    }
}
