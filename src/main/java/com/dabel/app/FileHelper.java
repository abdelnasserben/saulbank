package com.dabel.app;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {

    public static void save(MultipartFile file, String name) throws IOException {
        if(!file.isEmpty()) {
            Path path = Paths.get("src/main/resources/static/uploads/" + name);
            Files.write(path, file.getBytes());
        }
    }
}
