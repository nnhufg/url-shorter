package org.example;

import org.example.controller.ConsoleController;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        ConsoleController controller = new ConsoleController();

        controller.run();
    }
}
