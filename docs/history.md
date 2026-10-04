# 변경 이력

## 2026-10-04 이미지 저장 용량 제한과 실패 처리

- 운영 이미지 디렉터리를 5GB 루프백 ext4(`/srv/jandi-plan.img`, `/srv/jandi-plan`에 마운트)로 제한했다.
- 이미지 저장 실패(용량 부족, 권한)를 HTTP 200과 `imageUrl: null` 대신 예외(HTTP 500)로 응답하도록 변경했다.
- 이미지 교체 경로는 새 파일을 저장한 뒤에 기존 이미지를 삭제하도록 순서를 바꿨다.

## 2026-10-04 이미지 저장소 운영 전환

- 이미지 저장 디렉터리를 `home-server` 저장소 밖의 `/srv/jandi-plan`으로 정했다. 백업 이미지 170개를 복원했다.
- `image` 테이블에서 파일이 없는 행 13개를 삭제했다. 전환 전부터 GCS에도 파일이 없던 행이다.
- `community.contents`의 옛 GCS 주소 7건을 `https://plan-be.yeonjae.kr/images/`로 치환했다.
- 운영 설정의 `app.verify.url`을 Cloud Run 주소에서 `https://plan-be.yeonjae.kr/api/users/verify`로 바꿨다.
- GCS 버킷과 서비스 계정 키를 삭제했다.
- 운영 API로 업로드, 교체, 삭제, 확장자 거부(HTTP 400), 용량 제한(nginx 6MB)을 확인했다.

## 2026-10-04 이미지 저장소를 GCS에서 로컬 디스크로 전환

- 이미지 저장·삭제를 `LocalImageStorageService`로 변경했다.
- 공개 URL 접두사를 `image-prefix` 하나로 통합했다.
- 업로드 확장자를 `jpg`, `jpeg`, `png`, `gif`, `webp`로 제한했다. 그 밖의 확장자는 HTTP 400으로 거부한다.
- 이미지 교체 경로는 기존 이미지를 지우기 전에 업로드 파일을 검증하도록 변경했다.
- GCP 의존성(`spring-cloud-gcp-starter-storage`, `spring-cloud-gcp-starter-secretmanager`)과 `GcpCredentialsConfig`를 제거했다.
- 설계: `docs/superpowers/specs/2026-10-04-local-image-storage-design.md`
