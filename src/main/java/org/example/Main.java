package org.example;

import org.example.controller.ConsoleController;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            ConsoleController controller = new ConsoleController();
            controller.run();
        } catch (IOException e) {
            System.out.println("Ошибка работы с файлами: " + e.getMessage());
        }
    }
}
