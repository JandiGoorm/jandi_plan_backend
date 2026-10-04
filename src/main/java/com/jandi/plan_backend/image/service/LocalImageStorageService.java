package com.jandi.plan_backend.image.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 호스트 볼륨의 로컬 디렉터리에 이미지를 저장하고 삭제하는 서비스.
 */
@Slf4j
@Service
public class LocalImageStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path storageDir;

    public LocalImageStorageService(@Value("${image.storage-path}") String storagePath) {
        this.storageDir = Path.of(storagePath).toAbsolutePath().normalize();
    }

    // 마운트 누락 시 컨테이너 내부 저장 방지
    @PostConstruct
    void verifyStorageDir() {
        if (!Files.isDirectory(storageDir) || !Files.isWritable(storageDir)) {
            throw new IllegalStateException("이미지 저장 디렉터리를 쓸 수 없습니다: " + storageDir);
        }
    }

    /**
     * 파일을 저장하고, 성공하면 URL 인코딩된 파일명을 포함한 결과 문자열을 반환합니다.
     */
    public String uploadFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("파일의 이름이 유효하지 않습니다.");
        }

        String safeName = lastSegment(originalFileName);
        if (!hasAllowedExtension(safeName)) {
            log.warn("허용되지 않는 파일 형식: {}", safeName);
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp)");
        }

        String fileName = UUID.randomUUID() + "_" + safeName;
        Path target = resolveInStorageDir(fileName);
        Path temp = null;
        try {
            temp = Files.createTempFile(storageDir, ".upload-", ".tmp");
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            makeWorldReadable(temp);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            log.info("파일 저장 완료: {}", fileName);

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return "파일 업로드 성공: " + encodedFileName;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            return "파일 업로드 실패: " + e.getMessage();
        } finally {
            deleteQuietly(temp);
        }
    }

    /**
     * DB에 인코딩된 형태로 저장된 파일명을 디코딩해 파일을 삭제합니다.
     */
    public boolean deleteFile(String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
            boolean deleted = Files.deleteIfExists(resolveInStorageDir(decodedFileName));
            if (deleted) {
                log.info("파일 삭제 성공: {}", decodedFileName);
            } else {
                log.warn("파일 삭제 실패 (파일이 존재하지 않음?): {}", decodedFileName);
            }
            return deleted;
        } catch (Exception e) {
            log.error("파일 삭제 중 예외 발생: {}", e.getMessage());
            return false;
        }
    }

    // 경로 구분자 이후만 사용
    private static String lastSegment(String name) {
        String normalized = name.replace('\\', '/');
        String segment = normalized.substring(normalized.lastIndexOf('/') + 1).replace("\0", "").strip();
        if (segment.isEmpty()) {
            throw new IllegalArgumentException("파일의 이름이 유효하지 않습니다.");
        }
        return segment;
    }

    // nginx가 확장자로 Content-Type을 정하므로 확장자로 제한
    private static boolean hasAllowedExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(fileName.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    private Path resolveInStorageDir(String fileName) {
        Path resolved = storageDir.resolve(fileName).normalize();
        if (!resolved.startsWith(storageDir) || resolved.equals(storageDir)) {
            throw new IllegalArgumentException("저장 경로를 벗어난 파일명입니다: " + fileName);
        }
        return resolved;
    }

    // 임시 파일 기본 권한(rw-------)이면 nginx가 403 반환
    private static void makeWorldReadable(Path path) throws IOException {
        try {
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-r--r--"));
        } catch (UnsupportedOperationException e) {
            log.debug("POSIX 권한을 지원하지 않는 파일 시스템: {}", path);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("임시 파일 삭제 실패: {}", path);
        }
    }
}
