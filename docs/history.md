# 변경 이력

## 2026-10-04 이미지 저장소를 GCS에서 로컬 디스크로 전환

- 이미지 저장·삭제를 `LocalImageStorageService`로 변경했다.
- 공개 URL 접두사를 `image-prefix` 하나로 통합했다.
- 업로드 확장자를 `jpg`, `jpeg`, `png`, `gif`, `webp`로 제한했다. 그 밖의 확장자는 HTTP 400으로 거부한다.
- GCP 의존성(`spring-cloud-gcp-starter-storage`, `spring-cloud-gcp-starter-secretmanager`)과 `GcpCredentialsConfig`를 제거했다.
- 설계: `docs/superpowers/specs/2026-10-04-local-image-storage-design.md`
