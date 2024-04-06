package com.dabel.service.storage;

import java.nio.file.Path;

public class ProfileFileStorageService implements FileStorageService {

    private static final String UPLOAD_DIR = "./src/main/resources/static/assets/avatars";

    @Override
    public Path getLocation() {
        return Path.of(UPLOAD_DIR);
    }
}
