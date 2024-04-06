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

public class FileStorageService {

    private static final String UPLOAD_DIR = "./src/main/resources/static/uploads/";

    public static String save(MultipartFile file, String prefixFilename) throws IOException {

        try {

            //TODO: check if file is empty
            if (file.isEmpty()) {
                throw new IllegalOperationException("Can't upload empty file");
            }

            //TODO: normalize the file path
            String uploadedFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String normalizedFilename = prefixFilename + uploadedFilename.substring(uploadedFilename.lastIndexOf("."));

            Path destinationFile = Paths.get(UPLOAD_DIR + normalizedFilename)
                    .normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return normalizedFilename;

        } catch (IOException ex) {
            throw new IllegalOperationException("Failed to store file.");
        }
    }
}
