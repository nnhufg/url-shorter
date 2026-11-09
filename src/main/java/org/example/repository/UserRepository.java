package org.example.repository;

import org.example.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRepository {

    private final List<User> users = new ArrayList<>();

    public void saveUser(User user) {
        users.add(user);
    }

    public User findById(UUID id) {
        User target = users.stream().filter(user -> user.getId().equals(id)).findFirst().orElse(null);
        return target;
    }

    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
