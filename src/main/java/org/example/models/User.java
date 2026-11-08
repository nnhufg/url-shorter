package org.example.models;

import java.util.UUID;

public class User {

    private final UUID id;

    public User() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }
}