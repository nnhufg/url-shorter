package org.example;

import org.example.controller.ConsoleController;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        try {

            ConsoleController controller = new ConsoleController();
            controller.mainMenu();

        } catch (IOException e) {

            System.out.println(
                    "\nНе удалось запустить приложение."
            );

            System.out.println(
                    "Причина: " + e.getMessage()
            );
        }
    }
}
