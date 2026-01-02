### 개발 주의 사항

#### 로컬 개발
1. `src/main/resources/application-dev.properties` 파일을 생성하여 필요한 환경 변수를 설정합니다.
2. 실행 시 환경 변수 `SPRING_PROFILES_ACTIVE=dev`를 설정하여 `dev` 프로파일을 활성화해야 합니다.
   - **IntelliJ**: `Run Configuration` -> `Environment variables` 에 `SPRING_PROFILES_ACTIVE=dev` 추가.
   - **VS Code**: `launch.json`의 `env` 섹션에 `"SPRING_PROFILES_ACTIVE": "dev"` 추가.
```
{
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot-PlanBackendApplication<jandi_plan_backend>",
            "request": "launch",
            "cwd": "${workspaceFolder}",
            "mainClass": "com.jandi.plan_backend.PlanBackendApplication",
            "projectName": "jandi_plan_backend",
            "args": "",
            "envFile": "${workspaceFolder}/.env",
            "env": {
                "SPRING_PROFILES_ACTIVE": "dev"
            }
        }
    ]
}
```
   - **Terminal**: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`

#### 운영 환경
- 루트 디렉터리에 `.env` 파일을 생성하여 사용합니다.
- 기본적으로 `application.properties`가 `.env`의 값을 읽어오도록 설정되어 있습니다.

### 코드 테스트 방법

```
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.jandi.plan_backend.user.service.UserServiceTest"

# 특정 테스트 메서드만 실행
./gradlew test --tests "com.jandi.plan_backend.user.service.UserServiceTest.{특정 메서드}"

# 테스트 + 상세 로그 출력
./gradlew test --info

# 실패한 테스트만 재실행
./gradlew test --rerun-tasks

# 빌드 캐시 무시하고 전체 재실행
./gradlew clean test
```

### 깃 커밋 메시지 컨벤션

1. 기본 포맷 (Format)

```
태그(스코프): 제목 (50자 내외)

- 본문 (선택 사항, 자세한 설명이 필요할 때만 작성)
```

2. 스코프 (Scope) - 위치 구분

```
be | Backend 관련 코드
infra | 배포, Docker, CI/CD 등
```

3. 태그 (Type) - 작업 성격

```
feat | 새로운 기능 추가 | API 개발, 버튼 추가
fix | 버그 수정 | 로직 오류 수정, 오타 수정
docs | 문서 수정 | README, Swagger, 주석 수정
style | 코드 포맷팅 (로직 변경 X) | 세미콜론 누락, 줄바꿈, 들여쓰기 정렬
refactor | 코드 리팩토링 | 기능 변경 없이 코드 구조 개선
test | 테스트 코드 | 테스트 코드 추가/수정 (프로덕션 코드 변경 X)
chore | 기타 잡무 | 빌드 설정, 패키지 매니저 설정, 라이브러리 추가
```