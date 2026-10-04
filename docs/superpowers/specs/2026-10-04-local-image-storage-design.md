# 이미지 저장소를 GCS에서 로컬 스토리지로 전환

## 목적

- `jandi_plan_backend`의 GCP(Cloud Storage) 의존성을 제거한다.
- 이미지의 저장과 읽기를 배포 서버(Oracle)의 로컬 디스크로 옮긴다.
- 이미지는 이미 백업되어 있다. 이 작업은 백업 복원과 코드 전환을 포함하고, 백업 생성은 포함하지 않는다.

## 성공 기준

1. 업로드, 수정, 삭제 API가 로컬 디스크를 대상으로 동작한다.
2. 기존 DB 레코드를 수정하지 않고 모든 이미지가 `https://plan-be.yeonjae.kr/images/{파일명}`으로 조회된다.
3. 코드, `build.gradle`, 설정 파일에 GCP 관련 항목(`google-cloud`, `gcs.*`, `storage.googleapis.com`)이 남지 않는다.
4. 컨테이너를 재배포해도 이미지가 유지된다.

## 범위

포함:

- `jandi_plan_backend`: 저장 서비스 교체, 이미지 URL 접두사 통합, 의존성과 설정 정리.
- `home-server`: compose 볼륨, nginx 정적 서빙, 운영 설정 파일.

제외:

- 이미지 리사이징, 썸네일, CDN.
- DB 스키마 변경.
- 이미지 디렉터리의 자동 백업 구성(별도 작업으로 `docs/todo.md`에 기록).

## 현재 구조

- `GoogleCloudStorageService`가 `Storage` 빈으로 GCS에 업로드와 삭제를 한다.
- 파일명은 `{UUID}_{원본명}`이다. DB(`image.image_url`)에는 URL 인코딩된 파일명만 저장하고, GCS 객체명은 인코딩 전 이름이다.
- 공개 URL은 `https://storage.googleapis.com/plan-storage/` + DB 파일명이다.
- 접두사를 `${image-prefix}`로 읽는 코드와 문자열로 하드코딩한 코드가 섞여 있다. 하드코딩 위치는 다음과 같다.
  - `ImageService`
  - `TripService`
  - `PreferTripService`
  - `UserService`
  - `UserCommunityDTO`

## 설계

### 데이터 흐름

```
업로드: 클라이언트 -> nginx -> jandi-plan -> {image.storage-path}/{UUID}_{원본명}
읽기:   클라이언트 -> nginx /images/{파일명} -> 같은 디렉터리(읽기 전용)
```

### 저장 위치

- 호스트 경로: `/opt/home-server/data/plan-images`
- `jandi-plan` 컨테이너: `/app/uploads`에 읽기/쓰기로 마운트한다.
- `nginx` 컨테이너: `/var/www/plan-images`에 읽기 전용으로 마운트한다.
- 로컬 compose에는 `jandi-plan`에 `../data/plan-images` 볼륨을 추가하고, 이 디렉터리를 서빙하는 `plan-images`(nginx, 호스트 포트 8094) 서비스를 추가한다. 로컬 compose에는 nginx 게이트웨이가 없기 때문이다.

### 백엔드

- `LocalImageStorageService`가 `GoogleCloudStorageService`를 대체한다. 공개 메서드는 `uploadFile(MultipartFile)`, `deleteFile(String)`이고 반환 규칙은 기존과 같다. `ImageService`의 호출부는 변경하지 않는다.
- 설정 키: `image.storage-path`(저장 디렉터리), `image-prefix`(공개 URL 접두사, 끝에 `/` 포함).
- 하드코딩된 접두사는 모두 `${image-prefix}` 주입으로 바꾼다.
- 저장 규칙:
  - 파일명: `{UUID}_{원본명}`. 원본명에서 경로 구분자(`/`, `\`)와 `..`를 제거한다.
  - 검증: 해석한 최종 경로가 `image.storage-path` 하위인지 확인하고, 아니면 업로드를 거부한다.
  - 쓰기: 같은 디렉터리의 임시 파일에 쓴 뒤 원자적 이동으로 확정한다. 실패 시 임시 파일을 삭제한다.
  - 삭제: DB 파일명을 URL 디코딩한 이름으로 같은 경로 검증을 거쳐 삭제한다. 파일이 없으면 기존처럼 `false`를 반환한다.
- 제거: `GcpCredentialsConfig`(`Storage` 빈), `build.gradle`의 `spring-cloud-gcp-starter-storage`, `spring-cloud-gcp-starter-secretmanager`, spring-cloud-gcp BOM, `gcs.bucket.name`, `gcp.credentials.key.base64`. Secret Manager 참조(`sm://`)는 코드와 설정에 없다.
- 업로드 확장자는 `jpg`, `jpeg`, `png`, `gif`, `webp`(대소문자 무시)만 허용한다. 그 밖의 확장자는 저장하지 않고 실패 메시지를 반환한다. nginx가 확장자로 `Content-Type`을 정하므로, HTML과 SVG가 API 도메인에서 실행되는 것을 막기 위한 조치다.
- 저장 디렉터리가 없거나 쓸 수 없으면 기동 시 실패시킨다. 마운트가 빠진 채 컨테이너 내부에 저장되는 사고를 막는다.
- 업로드 파일의 권한은 `rw-r--r--`로 맞춘다. 임시 파일의 기본 권한(`rw-------`)을 그대로 두면 nginx가 읽지 못해 403이 난다.

### nginx와 compose (home-server)

- `plan-be.yeonjae.kr` 서버 블록에 `location /images/`를 추가한다.
  - `root /var/www/plan;`을 쓰고 이미지 디렉터리를 `/var/www/plan/images`에 마운트한다(`alias`의 `try_files` 문제를 피한다).
  - 파일이 없으면 404를 반환하고 백엔드로 넘기지 않는다.
  - `Cache-Control: public, max-age=31536000, immutable`. 파일명에 UUID가 있어 내용이 바뀌지 않는다.
  - `X-Content-Type-Options: nosniff`와 `Content-Security-Policy: default-src 'none'; sandbox`를 붙인다. 업로드 파일이 API 도메인에서 서빙되므로, 스크립트가 든 파일이 실행되지 않게 한다.
  - CORS 헤더는 기존 `location /`과 같은 값을 적용한다.
- `client_max_body_size 6m`을 `plan-be` 서버 블록에 추가한다. 현재는 기본값 1MB라서, 백엔드 제한(5MB)보다 먼저 업로드가 막힐 수 있다. multipart 오버헤드 때문에 5MB 파일이 5m을 넘을 수 있어 6m으로 한다.
- `config/jandi-plan/application.properties`, `application-local.properties`에 `image-prefix`와 `image.storage-path`를 반영한다.

### 환경변수와 설정 파일

- 키를 추가하면 `.env.example`과 `application.properties.example`을 먼저 수정한다.
- 운영 설정 파일(`home-server`)은 그 뒤에 맞춘다.

## 전환 순서

1. 백업 이미지를 `plan-images`에 복원한다.
2. 대조 확인: DB의 모든 `image.image_url`을 디코딩해 실제 파일 존재를 확인하고, 누락 목록을 만든다. 누락이 있으면 전환을 중단한다.
3. 로컬 도커 스택에서 기능을 검증한다(아래 검증 항목).
4. 운영에 배포한다. `home-server`(볼륨, nginx)를 먼저 반영하고 백엔드를 배포한다.
5. 운영에서 기존 이미지와 새 업로드 이미지를 `/images/` URL로 조회해 확인한다.
6. 확인 기간 후 GCS 버킷과 서비스 계정을 삭제한다. 이 단계는 사용자가 직접 수행한다.

## 검증

도커 환경에서 로컬 스택을 다시 빌드한 뒤 호스트에서 실제 엔드포인트를 호출한다. `/health/ready` 200만으로 끝내지 않는다.

- 이미지 업로드 API 호출 후 반환된 URL로 파일을 받는다(200, 본문 일치).
- 이미지 수정 API 호출 후 기존 파일이 삭제되고 새 파일이 조회된다.
- 이미지 삭제 API 호출 후 URL이 404를 반환한다.
- 한글, 공백이 있는 파일명이 올바르게 저장되고 조회된다.
- 복원한 기존 이미지 몇 건이 DB 레코드의 URL로 조회된다.
- 5MB 이하 파일이 nginx를 통과하고, 5MB 초과 파일은 거부된다.

자동 테스트는 3개 이내로 한다. 기존 테스트가 이미 검증하는 동작은 추가하지 않는다.

1. 경로 조작 파일명(`../x`, `a/b`)이 저장 디렉터리 밖에 쓰이지 않는다.
2. 업로드한 파일을 삭제하면 디스크에서 사라진다.
3. 수정 시 기존 파일이 삭제되고 새 파일이 생긴다.

영향받는 기존 테스트(`GoogleCloudStorageService`를 사용하거나 모킹하는 테스트)는 같은 작업에서 수정한다.

## 위험과 대응

| 위험 | 대응 |
|---|---|
| 재배포 시 이미지 손실 | 호스트 볼륨 마운트. 컨테이너 내부 경로에 저장하지 않는다. |
| 복원 파일명과 DB 값 불일치 | 전환 순서 2단계의 대조 확인. 누락이 있으면 중단. |
| 경로 조작 업로드 | 파일명 정제와 최종 경로 검증. |
| 서버 디스크가 유일한 사본 | 이미지 디렉터리 백업을 `docs/todo.md`에 등록하고 별도 작업으로 진행. |
| 운영 배포 중 URL 불일치 | 볼륨과 nginx를 먼저 반영한 뒤 백엔드를 배포. |

## 문서 반영

- 이 작업에서 바뀌는 현재 구조 문서(`docs/structure/`)는 같은 작업에서 수정한다.
- 이력은 `docs/history.md`에, 미완료 항목(이미지 백업, GCS 삭제)은 `docs/todo.md`에 둔다.
