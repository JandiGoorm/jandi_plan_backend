package com.jandi.plan_backend.image.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LocalImageStorageServiceTest {

    private static final String SUCCESS = "파일 업로드 성공: ";

    @TempDir
    Path root;

    private Path storageDir;
    private LocalImageStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        storageDir = Files.createDirectory(root.resolve("images"));
        service = new LocalImageStorageService(storageDir.toString());
    }

    @Test
    void 경로_조작_파일명은_저장_디렉터리_밖을_쓰거나_지우지_않는다() throws IOException {
        Path secret = Files.writeString(root.resolve("secret.txt"), "keep");

        service.uploadFile(new MockMultipartFile("file", "../../evil.png", "image/png", new byte[]{1}));
        service.uploadFile(new MockMultipartFile("file", "a\\b.png", "image/png", new byte[]{1}));

        try (Stream<Path> files = Files.list(storageDir)) {
            List<String> names = files.map(p -> p.getFileName().toString()).sorted().toList();
            assertThat(names).hasSize(2);
            assertThat(names).anyMatch(n -> n.endsWith("_evil.png"));
            assertThat(names).anyMatch(n -> n.endsWith("_b.png"));
        }
        assertThat(root.resolve("evil.png")).doesNotExist();

        assertThat(service.deleteFile("..%2Fsecret.txt")).isFalse();
        assertThat(service.deleteFile(URLEncoder.encode(secret.toString(), StandardCharsets.UTF_8))).isFalse();
        assertThat(secret).exists();
    }

    @Test
    void 한글_공백_파일명은_업로드_후_인코딩값으로_삭제되고_읽기_권한이_열려_있다() throws IOException {
        byte[] content = {1, 2, 3};
        String result = service.uploadFile(
                new MockMultipartFile("file", "한글 사진 (1)+a.png", "image/png", content));

        assertThat(result).startsWith(SUCCESS);
        String encoded = result.substring(SUCCESS.length());
        assertThat(encoded).doesNotContain(" ").contains("%20").contains("%2B");

        Path stored = storageDir.resolve(URLDecoder.decode(encoded, StandardCharsets.UTF_8));
        assertThat(Files.readAllBytes(stored)).isEqualTo(content);
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(stored))).isEqualTo("rw-r--r--");

        assertThat(service.deleteFile(encoded)).isTrue();
        assertThat(stored).doesNotExist();
        assertThat(service.deleteFile(encoded)).isFalse();
    }

    @Test
    void 저장_디렉터리가_없으면_기동_검증이_실패한다() {
        LocalImageStorageService missing = new LocalImageStorageService(root.resolve("not-mounted").toString());

        assertThatThrownBy(missing::verifyStorageDir).isInstanceOf(IllegalStateException.class);
        assertThatCode(service::verifyStorageDir).doesNotThrowAnyException();
    }

    @Test
    void 파일명이_비어_있으면_업로드를_거부한다() {
        assertThatThrownBy(() -> service.uploadFile(new MockMultipartFile("file", "dir/", "image/png", new byte[]{1})))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 허용되지_않는_확장자는_저장하지_않고_예외를_던진다() throws IOException {
        for (String name : List.of("x.html", "x.svg", "x.png.html", "noext", "x.")) {
            assertThatThrownBy(() -> service.uploadFile(new MockMultipartFile("file", name, "text/html", new byte[]{1})))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(service.uploadFile(new MockMultipartFile("file", "PHOTO.JPG", "image/jpeg", new byte[]{1})))
                .startsWith(SUCCESS);
        try (Stream<Path> files = Files.list(storageDir)) {
            assertThat(files.map(p -> p.getFileName().toString()).toList()).hasSize(1);
        }
    }
}
