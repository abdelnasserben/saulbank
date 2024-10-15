package com.dabel.app;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class Logger {
    private final String filename;

    public Logger(String name) {
        this.filename = name + "_" + LocalDate.now() + ".txt";
    }

    public void log(String data) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(data + "\n");
        } catch (IOException e) {
            System.err.println("An error as occurred : " + e.getMessage());
        }
    }
}

