package com.dabel.service.storage;

import com.dabel.exception.IllegalOperationException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public interface FileStorageService {

    Path getLocation();

    default String store(MultipartFile file, String prefixFilename) {
        try {

            //TODO: check if file is empty
            if (file.isEmpty()) {
                throw new IllegalOperationException("Can't upload empty file");
            }

            //TODO: normalize the file path
            String uploadedFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String normalizedFilename = prefixFilename + uploadedFilename.substring(uploadedFilename.lastIndexOf("."));

            Path destinationFile = Paths.get(getLocation() + "/" + normalizedFilename)
                    .normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return normalizedFilename;

        } catch (IOException ex) {
            throw new IllegalOperationException("Failed to store file.");
        }
    }
}
