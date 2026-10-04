package com.jandi.plan_backend.image.service;

import com.jandi.plan_backend.image.dto.ImageRespDto;
import com.jandi.plan_backend.image.entity.Image;
import com.jandi.plan_backend.image.repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    private static final String PREFIX = "https://plan-be.example/images/";

    @Mock
    private ImageRepository imageRepository;

    @TempDir
    Path storageDir;

    @Test
    void 이미지를_수정하면_기존_파일은_지워지고_새_파일_URL이_반환된다() throws IOException {
        ImageService imageService = new ImageService(
                new LocalImageStorageService(storageDir.toString()), imageRepository, PREFIX);
        Files.writeString(storageDir.resolve("old.png"), "old");
        Image image = new Image();
        image.setImageUrl("old.png");
        when(imageRepository.findById(1)).thenReturn(Optional.of(image));
        when(imageRepository.save(any(Image.class))).thenAnswer(inv -> inv.getArgument(0));

        ImageRespDto resp = imageService.updateImage(
                1, new MockMultipartFile("file", "new.png", "image/png", new byte[]{9}));

        assertThat(storageDir.resolve("old.png")).doesNotExist();
        assertThat(image.getImageUrl()).endsWith("_new.png");
        assertThat(resp.getImageUrl()).isEqualTo(PREFIX + image.getImageUrl());
        assertThat(storageDir.resolve(URLDecoder.decode(image.getImageUrl(), StandardCharsets.UTF_8))).exists();
    }

    @Test
    void 거부될_파일로_수정하면_기존_파일과_DB값이_그대로_남는다() throws IOException {
        ImageService imageService = new ImageService(
                new LocalImageStorageService(storageDir.toString()), imageRepository, PREFIX);
        Files.writeString(storageDir.resolve("old.png"), "old");
        Image image = new Image();
        image.setImageUrl("old.png");
        when(imageRepository.findById(1)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> imageService.updateImage(
                1, new MockMultipartFile("file", "x.html", "text/html", new byte[]{9})))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(storageDir.resolve("old.png")).exists();
        assertThat(image.getImageUrl()).isEqualTo("old.png");
    }
}
