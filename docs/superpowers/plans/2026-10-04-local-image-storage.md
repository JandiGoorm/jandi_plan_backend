# 로컬 이미지 저장소 전환 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `jandi_plan_backend`의 이미지 저장과 읽기를 GCS에서 Oracle 서버의 로컬 디스크(nginx 정적 서빙)로 옮기고, GCP 의존성을 제거한다.

**Architecture:** `LocalImageStorageService`가 호스트 볼륨(`/app/uploads`)에 파일을 쓰고, nginx가 같은 디렉터리를 읽기 전용으로 `/images/`에 서빙한다. 공개 URL 접두사는 `${image-prefix}` 하나로 통합하고, 모든 URL 조립은 `ImageService.toPublicUrl()`을 거친다. DB 스키마와 기존 레코드는 바꾸지 않는다.

**Tech Stack:** Java 21, Spring Boot 3.4.2, JUnit 5, Mockito, Docker, nginx 1.25.

**Spec:** `docs/superpowers/specs/2026-10-04-local-image-storage-design.md`

## Global Constraints

- 모든 텍스트 파일의 줄바꿈은 LF. CRLF가 있는 기존 파일은 우회 변환하지 말고 사용자에게 알린다.
- `jandi_plan_backend` 작업 브랜치: `feature/local-image-storage`(`origin/dev` 기준). `dev`에 직접 커밋·푸시하지 않는다. 푸시는 사용자 승인 후에만 한다.
- 작업 트리에 이미 `M AGENTS.md`가 있다. 이 파일은 수정·스테이지하지 않는다. `git add`는 항상 파일을 하나씩 지정한다.
- 빌드·테스트는 도커에서 실행한다. 동작 검증은 `/health/ready` 200만으로 끝내지 않고 실제 기능 엔드포인트를 호출한다.
- 주석은 코드에서 바로 읽히지 않는 것만, 블록당 3줄 이하, 명사구 단위. 미완료 항목은 `docs/todo.md`, 이력은 `docs/history.md`에 적는다.
- 저장 디렉터리 설정 키: `image.storage-path`(컨테이너 값 `/app/uploads`). 공개 URL 접두사: `image-prefix`(운영 값 `https://plan-be.yeonjae.kr/images/`, 끝에 `/`).
- 호스트 이미지 디렉터리: `/srv/jandi-plan`(`home-server` 저장소 밖). 로컬 개발은 `home-server/data/plan-images`를 쓴다.
- nginx: `client_max_body_size 6m`, `/images/` 응답에 `Cache-Control: public, max-age=31536000, immutable`, `X-Content-Type-Options: nosniff`, `Content-Security-Policy: default-src 'none'; sandbox`.
- 업로드 확장자 허용 목록: `jpg`, `jpeg`, `png`, `gif`, `webp`(대소문자 무시). SVG와 HTML은 허용하지 않는다.
- 업로드 파일 권한은 `rw-r--r--`. 업로드 메서드 반환 문자열 형식(`파일 업로드 성공: {인코딩된 파일명}`)은 바꾸지 않는다.
- 비밀 값(서비스 계정 키, 클라이언트 시크릿)을 출력·로그·커밋 메시지에 옮기지 않는다.

## Review Focus

1. 업로드 파일이 `rw-------`로 남아 nginx가 403을 반환한다(임시 파일 기본 권한). → Task 1의 업로드·삭제 테스트가 권한을 확인한다.
2. `../`, `\`, 절대 경로가 든 파일명으로 저장 디렉터리 밖을 쓰거나 지운다. → Task 1의 경로 조작 테스트.
3. 볼륨 마운트가 빠진 채 기동해 컨테이너 내부에 저장하고, 재배포 때 이미지가 사라진다. → Task 1의 기동 검증 테스트.
4. 사용자가 올린 `.html`, `.svg` 파일이 API 도메인에서 실행된다. → Task 1의 확장자 허용 목록 테스트와 Task 5의 nginx 헤더·curl 확인.
5. 한글·공백·`+`가 든 파일명이 DB 인코딩값과 복원 파일명 사이에서 어긋난다(macOS 백업의 NFD 이름 포함). → Task 1의 왕복 테스트와 Task 7의 대조 스크립트.
6. 기존 게시글 본문(`community.contents`)에 옛 접두사가 전체 URL로 박혀 있다. 치환하지 않으면 새 접두사로 본문을 검사하는 `ImageCleanupService`가 게시글 수정 때 첨부 이미지를 전부 미사용으로 판단해 삭제하고, GCS 삭제 후에는 본문의 이미지가 깨진다. → Task 7 Step 5, 7의 URL 치환.

---

## 작업 순서와 저장소

| Task | 저장소 | 내용 |
|---|---|---|
| 0 | jandi_plan_backend | 기준선 테스트 |
| 1 | jandi_plan_backend | `LocalImageStorageService` + 테스트 |
| 2 | jandi_plan_backend | `ImageService` 연결, URL 접두사 통합 |
| 3 | jandi_plan_backend | GCP 코드·의존성 제거 |
| 4 | jandi_plan_backend | 문서 |
| 5 | home-server | compose 볼륨, nginx, 로컬 `plan-images` |
| 6 | home-server | 운영·로컬 설정 파일(전환 시점에 적용) |
| 7 | Oracle 서버 | 디렉터리 생성, 복원, 대조, 전환 |

도커 테스트 실행 명령(이하 `GRADLE_TEST`):

```bash
cd /c/Users/serial/source/개인프로젝트/jandi_plan_backend
MSYS_NO_PATHCONV=1 docker run --rm -v "$(pwd -W):/app" -v plan-gradle-cache:/root/.gradle -w /app \
  eclipse-temurin:21-jdk-jammy bash gradlew test --no-daemon <옵션>
```

---

### Task 0: 기준선 테스트

**Files:** 없음(읽기 전용)

- [ ] **Step 1: 변경 전 전체 테스트 실행**

`GRADLE_TEST`를 옵션 없이 실행한다.
Expected: 결과를 기록한다. `PlanBackendApplicationTests.contextLoads`는 DB와 설정 파일이 없으면 이미 실패할 수 있다. 실패 목록을 `/tmp/baseline-failures.txt`에 적어 두고, 이후 Task의 실패와 비교한다.

---

### Task 1: LocalImageStorageService

**Files:**
- Create: `src/main/java/com/jandi/plan_backend/image/service/LocalImageStorageService.java`
- Test: `src/test/java/com/jandi/plan_backend/image/service/LocalImageStorageServiceTest.java`

**Interfaces:**
- Produces: `LocalImageStorageService(String storagePath)` (Spring: `@Value("${image.storage-path}")`)
  - `String uploadFile(MultipartFile file)` — 성공 `"파일 업로드 성공: " + 인코딩된 파일명`, 실패 `"파일 업로드 실패: " + 메시지`. 이름이 비어 있으면 `IllegalArgumentException`. 확장자가 허용 목록에 없으면 저장하지 않고 `IllegalArgumentException`을 던진다. `GlobalExceptionHandler`가 HTTP 400으로 응답한다.
  - `boolean deleteFile(String encodedFileName)` — 삭제하면 `true`. 파일이 없거나 저장 디렉터리 밖이면 `false`.
  - `void verifyStorageDir()` — 디렉터리가 없거나 쓸 수 없으면 `IllegalStateException`. `@PostConstruct`.

테스트는 5개다(권장 3개 초과). 기동 검증 테스트는 Review Focus 3번(마운트 누락), 확장자 테스트는 Review Focus 4번(HTML·SVG 실행)을 직접 잡는 유일한 테스트라서 추가한다.

- [ ] **Step 1: 실패하는 테스트 작성**

```java
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
```

`assertThatCode`를 쓰므로 import에 `import static org.assertj.core.api.Assertions.assertThatCode;`를 추가한다.

- [ ] **Step 2: 테스트가 컴파일 실패로 실패하는지 확인**

`GRADLE_TEST --tests "com.jandi.plan_backend.image.service.LocalImageStorageServiceTest"`
Expected: FAIL — `LocalImageStorageService` 심볼을 찾을 수 없다.

- [ ] **Step 3: 구현**

```java
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
```

- [ ] **Step 4: 테스트 통과 확인**

`GRADLE_TEST --tests "com.jandi.plan_backend.image.service.LocalImageStorageServiceTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: 커밋**

```bash
git add src/main/java/com/jandi/plan_backend/image/service/LocalImageStorageService.java
git add src/test/java/com/jandi/plan_backend/image/service/LocalImageStorageServiceTest.java
git commit -m "$(cat <<'EOF'
feat: add local filesystem image storage service

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: ImageService 연결과 URL 접두사 통합

**Files:**
- Modify: `src/main/java/com/jandi/plan_backend/image/service/ImageService.java`
- Modify: `src/main/java/com/jandi/plan_backend/tripPlan/trip/service/TripService.java:47,486,491,496,509`
- Modify: `src/main/java/com/jandi/plan_backend/user/service/PreferTripService.java:34,63,136,170,194`
- Modify: `src/main/java/com/jandi/plan_backend/user/service/UserService.java:235`
- Modify: `src/main/java/com/jandi/plan_backend/commu/community/dto/UserCommunityDTO.java:25`
- Modify (주석): `image/controller/ImageController.java:69,86,115`, `commu/community/service/ImageCleanupService.java:30`
- Test: `src/test/java/com/jandi/plan_backend/image/service/ImageServiceTest.java`

**Interfaces:**
- Consumes: `LocalImageStorageService.uploadFile/deleteFile` (Task 1).
- Produces: `ImageService(LocalImageStorageService storageService, ImageRepository imageRepository, @Value("${image-prefix}") String urlPrefix)`, `String ImageService.toPublicUrl(String storedFileName)` = `urlPrefix + storedFileName`.

- [ ] **Step 1: 실패하는 테스트 작성** (수정 시 기존 파일 삭제와 새 파일 저장)

```java
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
}
```

- [ ] **Step 2: 실패 확인**

`GRADLE_TEST --tests "com.jandi.plan_backend.image.service.ImageServiceTest"`
Expected: FAIL — `ImageService` 생성자 시그니처 불일치(컴파일 오류).

- [ ] **Step 3: `ImageService` 수정**

`ImageService.java`에서 다음을 바꾼다.

1. import `org.springframework.beans.factory.annotation.Value;` 추가.
2. 필드와 생성자(18~25행)를 교체한다.

```java
    private final LocalImageStorageService storageService;
    private final ImageRepository imageRepository;
    private final String urlPrefix;

    public ImageService(LocalImageStorageService storageService,
                        ImageRepository imageRepository,
                        @Value("${image-prefix}") String urlPrefix) {
        this.storageService = storageService;
        this.imageRepository = imageRepository;
        this.urlPrefix = urlPrefix;
    }

    /**
     * DB에 저장된 파일명으로 공개 URL을 만듭니다.
     */
    public String toPublicUrl(String storedFileName) {
        return urlPrefix + storedFileName;
    }
```

3. `googleCloudStorageService.` 호출 4곳(`uploadFile` 2곳, `deleteFile` 2곳)을 `storageService.`로 바꾼다.
4. `urlPrefix + image.getImageUrl()`(51, 119행)과 `urlPrefix + img.getImageUrl()`(75행)을 `toPublicUrl(...)`로 바꾼다.
5. 주석을 맞춘다.
   - 90~92행 `기존 파일을 클라우드 스토리지에서 삭제한 후` → `기존 파일을 저장소에서 삭제한 후`
   - 127~129행과 139~141행의 `googleCloudStorageService.deleteFile(...) → 실제 GCS 삭제` 설명을 `storageService.deleteFile(rawFileName) → 로컬 파일 삭제`로 바꾸고, `GCS 파일명` 표현을 `저장된 파일명`으로 바꾼다.

- [ ] **Step 4: 다른 클래스의 하드코딩 접두사 교체**

| 파일 | 변경 |
|---|---|
| `TripService.java` | 47행 `urlPrefix` 필드 삭제. 486, 491, 496, 509행 `urlPrefix + img.getImageUrl()` → `imageService.toPublicUrl(img.getImageUrl())` |
| `PreferTripService.java` | 34행 `urlPrefix` 필드 삭제. 63, 136, 170, 194행 같은 방식으로 교체 |
| `UserService.java` | 235행 `"https://storage.googleapis.com/plan-storage/" + profileImage.getImageUrl()` → `imageService.toPublicUrl(profileImage.getImageUrl())` |
| `UserCommunityDTO.java` | 25행 `"https://storage.googleapis.com/plan-storage/" + img.getImageUrl()` → `imageService.toPublicUrl(img.getImageUrl())` |

주석 수정:
- `ImageController.java:69` `{"imageUrl": "https://storage.googleapis.com/plan-storage/{파일명}"}` → `{"imageUrl": "{image-prefix}{파일명}"}`
- `ImageController.java:86,115`의 `클라우드 스토리지` → `저장소`
- `ImageCleanupService.java:30` 예시 URL → `예: "{image-prefix}encodedFileName.jpg"에서 "encodedFileName.jpg" 추출`

- [ ] **Step 5: 잔여 하드코딩 확인**

Run: `grep -rn "storage.googleapis\|new ImageService(\|GoogleCloudStorageService" src/main src/test`
Expected: `GoogleCloudStorageService.java` 자체와 `GcpCredentialsConfig` 외에 매칭 없음(두 파일은 Task 3에서 삭제).

- [ ] **Step 6: 영향 범위 테스트**

`GRADLE_TEST --tests "com.jandi.plan_backend.image.*" --tests "*TripServiceTest" --tests "*CommentQueryServiceTest" --tests "*CommentUpdateServiceTest" --tests "*CommunityQueryServiceTest" --tests "*CommunityUpdateServiceTest"`
Expected: PASS. 기존 테스트는 `ImageService`를 `@Mock`으로 쓰므로 생성자 변경의 영향이 없다.

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/jandi/plan_backend/image/service/ImageService.java
git add src/main/java/com/jandi/plan_backend/tripPlan/trip/service/TripService.java
git add src/main/java/com/jandi/plan_backend/user/service/PreferTripService.java
git add src/main/java/com/jandi/plan_backend/user/service/UserService.java
git add src/main/java/com/jandi/plan_backend/commu/community/dto/UserCommunityDTO.java
git add src/main/java/com/jandi/plan_backend/image/controller/ImageController.java
git add src/main/java/com/jandi/plan_backend/commu/community/service/ImageCleanupService.java
git add src/test/java/com/jandi/plan_backend/image/service/ImageServiceTest.java
git commit -m "$(cat <<'EOF'
refactor: route image URLs through ImageService.toPublicUrl

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: GCP 코드와 의존성 제거

**Files:**
- Delete: `src/main/java/com/jandi/plan_backend/image/service/GoogleCloudStorageService.java`
- Delete: `src/main/java/com/jandi/plan_backend/config/GcpCredentialsConfig.java`
- Modify: `build.gradle:7-9,47-49,68`
- Modify: `src/main/resources/application.properties.example:39-42,80`

**Interfaces:** Consumes: Task 2 이후 `GoogleCloudStorageService`를 참조하는 코드가 없어야 한다.

- [ ] **Step 1: 파일 삭제**

```bash
git rm src/main/java/com/jandi/plan_backend/image/service/GoogleCloudStorageService.java
git rm src/main/java/com/jandi/plan_backend/config/GcpCredentialsConfig.java
```

- [ ] **Step 2: `build.gradle` 수정**

- 7~9행 `ext { springCloudGcpVersion = "6.0.0" }` 블록 삭제.
- 47~49행 `// GCP` 주석과 `spring-cloud-gcp-starter-storage`, `spring-cloud-gcp-starter-secretmanager` 두 줄 삭제.
- 68행 `mavenBom "com.google.cloud:spring-cloud-gcp-dependencies:$springCloudGcpVersion"` 삭제. `dependencyManagement` 블록에 다른 BOM이 없으면 블록도 삭제한다.
- `google-api-client`, `google-maps-services`는 유지한다(소셜 로그인, 장소 추천에서 사용).

- [ ] **Step 3: `application.properties.example` 수정**

`# Google Cloud Storage` 구획(39~42행)을 다음으로 바꾼다.

```properties
# ===========================================
# Image Storage
# ===========================================
image.storage-path=/app/uploads
```

`image-prefix`(80행)를 `image-prefix=https://your-domain.com/images/`로 바꾼다.

- [ ] **Step 4: 잔여 GCP 참조 확인**

Run: `grep -rnE "storage.googleapis|spring-cloud-gcp|com.google.cloud|gcs\.|gcp\.|GCP_SA_KEY" src build.gradle`
Expected: 매칭 없음. (`google.api.key`, `oauth2.googleapis.com` 같은 Google API 항목은 대상이 아니다.)

- [ ] **Step 5: 의존성 트리 확인**

`MSYS_NO_PATHCONV=1 docker run --rm -v "$(pwd -W):/app" -v plan-gradle-cache:/root/.gradle -w /app eclipse-temurin:21-jdk-jammy bash gradlew dependencies --configuration runtimeClasspath --no-daemon | grep -iE "google-cloud|spring-cloud-gcp"`
Expected: 출력 없음.

- [ ] **Step 6: 전체 빌드와 테스트**

`GRADLE_TEST` (옵션 없음). Expected: Task 0 기준선과 같은 실패만 남는다. 새 실패가 있으면 중단하고 원인을 확인한다. 이어서 `docker build -t jandi-plan-local .` 가 성공하는지 확인한다.

- [ ] **Step 7: 커밋**

```bash
git add build.gradle
git add src/main/resources/application.properties.example
git commit -m "$(cat <<'EOF'
chore: remove GCP storage dependencies and config

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

(`git rm`으로 삭제한 두 파일은 이미 스테이지되어 같은 커밋에 들어간다.)

---

### Task 4: 문서

**Files:**
- Modify: `README.md:23,66`
- Modify: `docs/structure/Specification.md:163`
- Create: `docs/history.md`
- Create: `docs/todo.md`

- [ ] **Step 1: 현재 상태 문서 수정**

- `README.md:23` `| **Storage** | Google Cloud Storage |` → `| **Storage** | 서버 로컬 디스크 (nginx 정적 서빙) |`
- `README.md:66` `DB, GCS, OAuth 정보 입력` → `DB, 이미지 저장 경로(image.storage-path), OAuth 정보 입력`
- `docs/structure/Specification.md:163` `이미지 업로드 → GCS 저장` → `이미지 업로드 → 로컬 디스크 저장`

- [ ] **Step 2: `docs/history.md` 작성**

```markdown
# 변경 이력

## 2026-10-04 이미지 저장소를 GCS에서 로컬 디스크로 전환

- 이미지 저장·삭제를 `LocalImageStorageService`로 변경했다.
- 공개 URL 접두사를 `image-prefix` 하나로 통합했다.
- 업로드 확장자를 `jpg`, `jpeg`, `png`, `gif`, `webp`로 제한했다.
- GCP 의존성(`spring-cloud-gcp-starter-storage`, `spring-cloud-gcp-starter-secretmanager`)과 `GcpCredentialsConfig`를 제거했다.
- 설계: `docs/superpowers/specs/2026-10-04-local-image-storage-design.md`
```

- [ ] **Step 3: `docs/todo.md` 작성**

```markdown
# 미완료 항목

- 이미지 디렉터리(`/srv/jandi-plan`) 정기 백업 구성.
- 전환 확인 후 GCS 버킷(`plan-storage`)과 서비스 계정 삭제. 서비스 계정 키가 `home-server` 저장소 이력에 있으므로 키를 폐기한다.
- DB 저장 실패 시 디스크에 남는 고아 파일 정리. `image.image_url` 길이 제한은 100자다.
```

- [ ] **Step 4: 커밋**

```bash
git add README.md docs/structure/Specification.md docs/history.md docs/todo.md
git commit -m "$(cat <<'EOF'
docs: update storage docs, add history and todo

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 5: home-server — compose 볼륨, nginx, 로컬 plan-images

**저장소:** `C:\Users\serial\source\개인프로젝트\home-server` (현재 `main`). 이 저장소에는 `AGENTS.md`가 없다. 작업 시작 시 `git checkout -b feature/local-image-storage`로 브랜치를 만들고, 푸시하지 않는다.

**Files:**
- Modify: `docker/docker-compose.prod.yml:75-85`
- Modify: `docker/docker-compose.local.yml:115-125`
- Modify: `nginx/conf.d/jandi.conf` (plan-be 블록)
- Create: `nginx/local/plan-images.conf`
- Modify: `ARCHITECTURE.md:306-310`

- [ ] **Step 1: 운영 compose 수정**

`jandi-plan.volumes`에 한 줄 추가한다.

```yaml
  jandi-plan:
    volumes:
      - /opt/home-server/config/jandi-plan:/app/config:ro
      - /srv/jandi-plan:/app/uploads
```

`nginx.volumes`에 한 줄 추가한다.

```yaml
      - /srv/jandi-plan:/var/www/plan/images:ro
```

- [ ] **Step 2: 로컬 compose 수정**

`jandi-plan.volumes`에 `- ../data/plan-images:/app/uploads`를 추가하고, `jandi-plan` 서비스 아래(`jandi-band-py` 앞)에 서비스를 추가한다.

```yaml
  plan-images:
    image: nginx:1.25-alpine
    container_name: plan-images
    volumes:
      - ../data/plan-images:/var/www/plan/images:ro
      - ../nginx/local/plan-images.conf:/etc/nginx/conf.d/default.conf:ro
    ports:
      - "8094:80"
    restart: unless-stopped
```

- [ ] **Step 3: `nginx/local/plan-images.conf` 작성**

```nginx
server {
    listen 80;
    root /var/www/plan;

    location /images/ {
        add_header Cache-Control "public, max-age=31536000, immutable" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header Content-Security-Policy "default-src 'none'; sandbox" always;
        add_header Access-Control-Allow-Origin "*" always;
    }
}
```

- [ ] **Step 4: 운영 nginx 설정 수정**

`nginx/conf.d/jandi.conf`의 Jandi Plan Backend 443 블록에서 아래 부분을 찾아 교체한다(`server_name plan-be.yeonjae.kr;` 다음에 빈 줄이 오는 쪽이 443 블록이다).

기존:

```nginx
    server_name plan-be.yeonjae.kr;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
```

변경:

```nginx
    server_name plan-be.yeonjae.kr;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    client_max_body_size 6m;

    # 업로드 이미지 정적 서빙 (/var/www/plan/images)
    location /images/ {
        root /var/www/plan;
        add_header Cache-Control "public, max-age=31536000, immutable" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header Content-Security-Policy "default-src 'none'; sandbox" always;
        add_header Access-Control-Allow-Origin $cors_origin always;
        add_header Access-Control-Allow-Credentials 'true' always;
    }

    location / {
```

- [ ] **Step 5: `ARCHITECTURE.md` 데이터 구조 수정**

`ARCHITECTURE.md`의 `data/` 목록에서 `│   └── mongodb/`를 다음 두 줄로 바꾼다.

```
│   ├── mongodb/
│   └── plan-images/                 # jandi-plan 업로드 이미지 (nginx /images/ 서빙)
```

- [ ] **Step 6: nginx 설정 검증 (실제 conf로 임시 컨테이너 기동)**

```bash
cd /c/Users/serial/source/개인프로젝트/home-server
T=$(mktemp -d); TW=$(cygpath -m "$T")
mkdir -p "$T/ssl" "$T/images"
openssl req -x509 -newkey rsa:2048 -nodes -days 1 -subj "/CN=plan-be.yeonjae.kr" \
  -keyout "$T/ssl/privkey.pem" -out "$T/ssl/fullchain.pem"
printf 'PNGDATA' > "$T/images/test image.png"
printf '<script>alert(1)</script>' > "$T/images/x.html"
ARGS=""; for h in $(grep -rhoE 'proxy_pass +http://[a-z0-9-]+' nginx/conf.d | sed -E 's#proxy_pass +http://##' | sort -u); do ARGS="$ARGS --add-host $h:127.0.0.1"; done
MSYS_NO_PATHCONV=1 docker run -d --name plan-nginx-test $ARGS -p 18443:443 \
  -v "$(pwd -W)/nginx/nginx.conf:/etc/nginx/nginx.conf:ro" \
  -v "$(pwd -W)/nginx/conf.d:/etc/nginx/conf.d:ro" \
  -v "$TW/ssl:/etc/nginx/ssl:ro" -v "$TW/images:/var/www/plan/images:ro" nginx:1.25-alpine
sleep 2; docker logs plan-nginx-test 2>&1 | tail -5
```

Expected: 컨테이너가 실행 중이다. 기동 실패 시 로그의 오류가 `jandi.conf`가 아닌 다른 설정 때문이면 사용자에게 보고한다.

```bash
R="--resolve plan-be.yeonjae.kr:18443:127.0.0.1"; U=https://plan-be.yeonjae.kr:18443
curl -sk $R -D - -o /dev/null "$U/images/test%20image.png"
curl -sk $R -D - -o /dev/null "$U/images/x.html"
curl -sk $R -o /dev/null -w '%{http_code}\n' "$U/images/missing.png"
head -c 5500000 /dev/zero | curl -sk $R -X POST --data-binary @- -o /dev/null -w '%{http_code}\n' "$U/api/images/upload"
head -c 7000000 /dev/zero | curl -sk $R -X POST --data-binary @- -o /dev/null -w '%{http_code}\n' "$U/api/images/upload"
```

Expected:
- 첫 번째: `200`, `Cache-Control: public, max-age=31536000, immutable`, `X-Content-Type-Options: nosniff`.
- 두 번째: `200`, `Content-Security-Policy: default-src 'none'; sandbox`.
- 세 번째: `404`.
- 네 번째(5.5MB): `502` — nginx를 통과해 백엔드 연결 단계에서 실패한 것이다.
- 다섯 번째(7MB): `413`.

정리: `docker rm -f plan-nginx-test; rm -rf "$T"`

- [ ] **Step 7: 커밋**

```bash
git checkout -b feature/local-image-storage
git add docker/docker-compose.prod.yml docker/docker-compose.local.yml
git add nginx/conf.d/jandi.conf nginx/local/plan-images.conf ARCHITECTURE.md
git commit -m "$(cat <<'EOF'
feat: serve plan images from local volume via nginx

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

---

### Task 6: home-server — 설정 파일 (전환 시점에 적용)

이 변경은 백엔드 배포와 같은 시점에 서버에 반영한다. 먼저 반영하면 구버전 백엔드가 새 URL 접두사를 쓰게 되어 이미지가 깨진다.

**Files:**
- Modify: `config/jandi-plan/application.properties`
- Modify: `config/jandi-plan/application-local.properties`

- [ ] **Step 1: 줄바꿈 확인**

Run: `grep -c $'\r' config/jandi-plan/application.properties config/jandi-plan/application-local.properties`
Expected: `0`. 0이 아니면 변환하지 말고 사용자에게 알린다.

- [ ] **Step 2: GCP 항목 삭제와 새 키 추가**

```bash
cd /c/Users/serial/source/개인프로젝트/home-server/config/jandi-plan
sed -i '/^gcp\.credentials\.key\.base64=/d; /^gcs\.bucket\.name=/d; /^#Google Cloud Storage$/d' application.properties application-local.properties
sed -i 's#^image-prefix=.*#image-prefix=https://plan-be.yeonjae.kr/images/\nimage.storage-path=/app/uploads#' application.properties
sed -i 's#^image-prefix=.*#image-prefix=http://localhost:8094/images/\nimage.storage-path=/app/uploads#' application-local.properties
```

인증 메일 링크(`app.verify.url`)가 쓰지 않는 Cloud Run 주소를 가리킨다. 운영 도메인으로 바꾸고, 같은 값을 적은 주석 줄을 지운다.

```bash
sed -i 's#^app\.verify\.url=https://planbackend-.*run\.app/#app.verify.url=https://plan-be.yeonjae.kr/#; /^# 테스트용 app\.verify\.url=/d' application.properties
```

- [ ] **Step 3: 결과 확인 (비밀 값을 출력하지 않는다)**

Run: `grep -nE "^(image-prefix|image\.storage-path)=" application.properties application-local.properties; grep -cE "gcp\.|gcs\.|storage\.googleapis|run\.app" application.properties application-local.properties; grep -n "^app.verify.url=" application.properties`
Expected: 각 파일에 `image-prefix`와 `image.storage-path` 한 줄씩. 두 번째 명령은 `0` 두 개. 세 번째 명령은 `app.verify.url=https://plan-be.yeonjae.kr/api/users/verify`.

- [ ] **Step 4: 커밋**

```bash
git add config/jandi-plan/application.properties config/jandi-plan/application-local.properties
git commit -m "$(cat <<'EOF'
chore: switch jandi-plan image settings to local storage

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
EOF
)"
```

- [ ] **Step 5: 로컬 통합 검증 (AGENTS.md 테스트 규칙 11)**

1. 백엔드 이미지를 새로 빌드한다: `docker build -t ghcr.io/kyj0503/jandi-plan:latest <jandi_plan_backend 경로>` (태그는 `docker-compose.yml`의 `jandi-plan` 이미지와 맞춘다).
2. `docker compose -f docker/docker-compose.yml -f docker/docker-compose.local.yml up -d mysql jandi-plan plan-images`
3. 로그인해서 토큰을 받고, 호스트에서 호출한다.
   - `POST http://localhost:8093/api/images/upload` (multipart: `file`, `targetId`, `targetType`) → 응답 `imageUrl`이 `http://localhost:8094/images/...`이다.
   - 그 `imageUrl`로 `curl -I` → `200`, `X-Content-Type-Options: nosniff`.
   - `PUT /api/images/{imageId}`로 수정 → 기존 파일이 `data/plan-images`에서 사라지고 새 URL이 `200`.
   - `DELETE /api/images/{imageId}` → URL이 `404`, DB 행이 삭제됨.
4. `ls -l data/plan-images`에서 파일 권한이 `-rw-r--r--`인지 확인한다.

Expected: 모두 위와 같다. 하나라도 다르면 중단하고 원인을 조사한다. DB 스키마가 필요하면 마이그레이션까지 맞춘다.

---

### Task 7: Oracle 서버 — 디렉터리 생성, 복원, 대조, 전환

**대상:** Oracle 서버(리눅스). 사용자가 직접 실행하거나 에이전트가 SSH로 실행한다. 이 Task의 명령은 서버 상태를 바꾸므로 실행 전에 사용자 승인을 받는다.

**Files:**
- Create: `home-server/scripts/check-plan-images.sh` (Task 5 브랜치에 포함해 커밋)

- [ ] **Step 1: 대조 스크립트 작성** — `scripts/check-plan-images.sh`

```bash
#!/bin/bash
set -e

# 사용법: check-plan-images.sh <image_url 목록 파일> <이미지 디렉터리>
LIST=$1
DIR=$2

if [ ! -f "$LIST" ] || [ ! -d "$DIR" ]; then
    echo "Usage: $0 <image_url 목록 파일> <이미지 디렉터리>"
    exit 1
fi

total=0
missing=0
while IFS= read -r encoded; do
    [ -z "$encoded" ] && continue
    total=$((total + 1))
    # Java URLDecoder와 같은 규칙(+ 는 공백)
    name=$(python3 -c 'import sys,urllib.parse;print(urllib.parse.unquote_plus(sys.argv[1]),end="")' "$encoded")
    if [ ! -f "$DIR/$name" ]; then
        echo "MISSING: $encoded"
        missing=$((missing + 1))
    fi
done < "$LIST"

echo "total=$total missing=$missing"
[ "$missing" -eq 0 ]
```

권한과 커밋: `chmod +x scripts/check-plan-images.sh && git add scripts/check-plan-images.sh && git commit -m "feat: add plan image reconcile script"` (메시지 끝에 Co-Authored-By 줄 포함).

- [ ] **Step 2: 서버 환경 확인**

```bash
readlink -f /opt/home-server      # home-server 체크아웃 실제 경로
getenforce 2>/dev/null || echo "SELinux 없음"
df -h /opt/home-server            # 이미지 용량 + 여유 공간
python3 --version
```

`getenforce`가 `Enforcing`이면 compose의 두 `plan-images` 마운트 끝에 `:z`를 붙여야 한다(`...:/app/uploads:z`, `...:/var/www/plan/images:ro,z`). 이 경우 Task 5 Step 1을 수정해 다시 커밋한다.

- [ ] **Step 3: 디렉터리 생성**

```bash
sudo mkdir -p /srv/jandi-plan
sudo chown ubuntu:ubuntu /srv/jandi-plan
chmod 755 /srv/jandi-plan
ls -ld /srv/jandi-plan
```

Expected: `drwxr-xr-x ubuntu ubuntu`. 백엔드 컨테이너가 root로 실행되므로 쓸 수 있다.

- [ ] **Step 4: 백업 복원**

```bash
sudo rsync -a --chmod=D755,F644 <백업 디렉터리>/ /srv/jandi-plan/
```

`<백업 디렉터리>`는 이미지 백업을 풀어 둔 경로다. 파일은 하위 디렉터리 없이 `plan-images` 바로 아래에 있어야 한다(GCS 객체명과 같은 평면 구조).

- [ ] **Step 5: DB 대조**

DB 호스트(`10.0.0.161`), 데이터베이스 `plan_backend`, 테이블 `image`다. 접속 계정은 `config/jandi-plan/application.properties`의 `spring.datasource.username`과 같다. 비밀번호는 명령에 직접 쓰지 말고 프롬프트(`-p`)로 입력한다.

```bash
mysql -h 10.0.0.161 -u <계정> -p plan_backend -N -B -e "SELECT image_url FROM image" > /tmp/image-urls.txt
/opt/home-server/scripts/check-plan-images.sh /tmp/image-urls.txt /srv/jandi-plan
```

Expected: `total=N missing=0`. 허용 목록 밖의 기존 파일도 확인한다: `grep -iEv '\.(jpe?g|png|gif|webp)$' /tmp/image-urls.txt`. 결과가 있으면(특히 `.svg`, `.html`) 사용자에게 보고한다. 기존 파일은 서빙되므로 nginx 헤더가 막아 주지만, 삭제 여부는 사용자가 정한다. 누락이 있으면 전환을 중단하고, 누락 목록의 원인을 확인한다(파일명 정규화 차이, 백업 누락 등). 누락 파일을 복원한 뒤 다시 실행해 `missing=0`이 될 때까지 반복한다.

- [ ] **Step 5-1: 본문에 박힌 옛 URL 범위 확인**

옛 접두사가 든 테이블과 건수를 확인한다. 2026-01-18 백업에서는 `community` 한 테이블에 17건이었다. 현재 DB 기준으로 다시 확인한다.

```bash
mysqldump -h 10.0.0.161 -u <계정> -p --single-transaction --skip-extended-insert plan_backend \
  | grep "storage.googleapis.com/plan-storage/" | cut -d'(' -f1 | sort | uniq -c
```

Expected: 목록에 나온 테이블이 치환 대상이다. `community` 외 테이블이 있으면 Step 7-4의 `UPDATE`에 같은 방식으로 추가하고, 사용자에게 알린다.

- [ ] **Step 6: nginx 선반영 (구버전 백엔드와 공존 가능)**

1. `home-server`의 Task 5 브랜치를 PR로 병합한다(사용자가 GitHub에서 merge).
2. 서버에서 `cd /opt/home-server && git pull`.
3. nginx만 새 볼륨으로 다시 만든다. 이 순간 모든 사이트가 몇 초간 끊긴다.

```bash
docker compose -f docker/docker-compose.yml -f docker/docker-compose.prod.yml up -d nginx
docker exec nginx-gateway nginx -t
```

4. 복원한 이미지 하나로 확인한다.

```bash
curl -sI "https://plan-be.yeonjae.kr/images/<복원한 파일의 인코딩 이름>"
```

Expected: `200`, `Cache-Control`, `X-Content-Type-Options` 헤더.

- [ ] **Step 7: 전환 (설정과 백엔드를 같은 시점에)**

1. Task 6 브랜치를 PR로 병합한다. 서버에서 `git pull`로 설정 파일을 받는다(실행 중인 구버전 컨테이너는 이미 설정을 읽었으므로 영향이 없다).
2. `jandi_plan_backend`의 `feature/local-image-storage` PR을 `dev`에 병합한다(사용자가 GitHub에서 merge). Jenkins가 `deploy-app.sh jandi-plan`으로 배포한다.
3. 기동 로그에서 `이미지 저장 디렉터리를 쓸 수 없습니다`가 없는지 확인한다: `docker logs jandi-plan 2>&1 | tail -50`
4. DB 본문 URL 치환. 배포 직후에 실행한다(구버전과 신버전 사이의 공백 시간에 옛 게시글을 수정하면 이미지가 삭제될 수 있으므로, 이용자가 적은 시간에 한다). 먼저 백업한다.

```bash
mysqldump -h 10.0.0.161 -u <계정> -p --single-transaction plan_backend community > /tmp/community-before-url-migration.sql
mysql -h 10.0.0.161 -u <계정> -p plan_backend -e "
SELECT COUNT(*) FROM community WHERE contents LIKE '%https://storage.googleapis.com/plan-storage/%';
UPDATE community
   SET contents = REPLACE(contents, 'https://storage.googleapis.com/plan-storage/', 'https://plan-be.yeonjae.kr/images/')
 WHERE contents LIKE '%https://storage.googleapis.com/plan-storage/%';
SELECT ROW_COUNT();
SELECT COUNT(*) FROM community WHERE contents LIKE '%storage.googleapis.com%';"
```

Expected: 첫 건수와 `ROW_COUNT()`가 같고, 마지막 건수는 `0`이다.

5. 운영 스모크 테스트:
   - 옛 게시글을 열면 본문 이미지가 보인다(주소가 `https://plan-be.yeonjae.kr/images/`로 시작).
   - 옛 게시글을 수정(내용 저장)한 뒤에도 첨부 이미지가 남아 있다(`image` 테이블에서 해당 `target_id` 행 확인).
   - `.html` 파일 업로드가 HTTP 400(`허용되지 않는 파일 형식입니다`)으로 거부된다.
   - 회원가입 인증 메일의 링크가 `https://plan-be.yeonjae.kr/api/users/verify`로 시작한다.
   - 기존 이미지가 있는 화면(프로필, 도시 대표 이미지)에서 이미지가 보인다.
   - `GET https://plan-be.yeonjae.kr/api/images/1`의 `imageUrl`이 `https://plan-be.yeonjae.kr/images/...`이고 `200`이다.
   - 새 이미지를 업로드하고 URL이 `200`이다. `ls -l /srv/jandi-plan | tail -3`에서 새 파일 권한이 `-rw-r--r--`이다.
   - 1MB를 넘는 이미지(최대 5MB) 업로드가 성공한다.

- [ ] **Step 8: 롤백 기준**

스모크 테스트가 실패하면 `/tmp/community-before-url-migration.sql`로 `community` 테이블을 복원하고 `config/jandi-plan`의 두 파일을 이전 커밋으로 되돌리고, 이전 `jandi-plan` 이미지 태그로 `deploy-app.sh jandi-plan`을 다시 실행한다. GCS 버킷은 확인 기간 동안 삭제하지 않으므로 구버전이 바로 동작한다.

- [ ] **Step 9: 사후 정리 (확인 기간 후, 사용자가 직접)**

GCS 버킷 `plan-storage` 삭제, 서비스 계정과 키 폐기. 키가 `home-server` 저장소 이력에 있으므로 폐기가 필요하다. 완료 후 `docs/todo.md`의 항목을 `docs/history.md`로 옮긴다.

---

## Self-Review

**Spec coverage**
- 백엔드 서비스 교체: Task 1. URL 접두사 통합: Task 2. GCP 제거(`GcpCredentialsConfig`, 의존성, 설정): Task 3.
- 볼륨·nginx·`client_max_body_size`·헤더·로컬 compose: Task 5. 운영·로컬 설정: Task 6.
- 전환 순서(복원, 대조, 로컬 검증, 배포, 정리): Task 6 Step 5, Task 7.
- 검증 항목: 업로드·수정·삭제(Task 6 Step 5), 한글·공백 파일명(Task 1), 기존 이미지 조회(Task 7 Step 7), 5MB 경계(Task 5 Step 6, Task 7 Step 7).
- 문서(`history.md`, `todo.md`, 구조 문서): Task 4, Task 5 Step 5.
- spec과 달라진 점: nginx `alias` → `root`, `5m` → `6m`, 기동 검증과 파일 권한, `GcpCredentialsConfig`·secretmanager 제거 범위. 이 내용은 spec에 이미 반영했다.

**Placeholder scan:** 코드가 필요한 단계에는 코드를 넣었다. `<백업 디렉터리>`, `<계정>`은 서버마다 다른 값이라 사용자가 채운다.

**Type consistency:** `LocalImageStorageService(String)`, `uploadFile`, `deleteFile`, `verifyStorageDir`, `ImageService(LocalImageStorageService, ImageRepository, String)`, `toPublicUrl(String)`의 이름과 시그니처가 Task 1, 2, 3에서 같다.
