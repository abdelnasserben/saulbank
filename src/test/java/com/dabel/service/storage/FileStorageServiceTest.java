package com.dabel.service.storage;

import com.dabel.exception.IllegalOperationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class FileStorageServiceTest {

    FileStorageService storageService = new ProfileFileStorageService();

    @Test
    void shouldSaveUploadedFileAndReturnNormalizedFilename() {
        //given
        MultipartFile multipartFile = new MockMultipartFile("file", "user.txt",
                "text/plain", "Spring Framework".getBytes());
        ///when
        String expected = storageService.store(multipartFile, "NBE452560");

        //then
        assertThat(expected).isEqualTo("NBE452560.txt");
    }

    @Test
    void shouldThrowExceptionWhenTryingStoreEmptyFile() {
        //given
        MultipartFile multipartFile = new MockMultipartFile("file", new byte[0]);

        //when
        Exception expected = assertThrows(IllegalOperationException.class, () -> storageService.store(multipartFile, "NBE410025"));

        //then
        assertThat(expected.getMessage()).isEqualTo("Can't upload empty file");
    }
}