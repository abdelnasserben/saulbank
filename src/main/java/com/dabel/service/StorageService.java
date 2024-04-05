package com.dabel.service;

import com.dabel.exception.IllegalOperationException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class StorageService {

    private static final String UPLOAD_DIR = "./src/main/resources/static/uploads/";

    public static String save(MultipartFile file, String prefixFilename) {
        //TODO: check if file is empty
        if (file.isEmpty()) {
            throw new IllegalOperationException("Can't upload empty file");
        }

        //TODO: normalize the file path
        String entireFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileName = prefixFilename + entireFilename.substring(entireFilename.lastIndexOf("."));

        //TODO: save the file on the local file system
        try {
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {}

        return fileName;
    }
}
