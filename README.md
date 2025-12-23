### 개발 주의 사항

#### 로컬 개발
1. `src/main/resources/application-dev.properties` 파일을 생성하여 필요한 환경 변수를 설정합니다.
2. 실행 시 환경 변수 `SPRING_PROFILES_ACTIVE=dev`를 설정하여 `dev` 프로파일을 활성화해야 합니다.
   - **IntelliJ**: `Run Configuration` -> `Environment variables` 에 `SPRING_PROFILES_ACTIVE=dev` 추가.
   - **VS Code**: `launch.json`의 `env` 섹션에 `"SPRING_PROFILES_ACTIVE": "dev"` 추가.
   - **Terminal**: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`

#### 운영 환경
- 루트 디렉터리에 `.env` 파일을 생성하여 사용합니다.
- 기본적으로 `application.properties`가 `.env`의 값을 읽어오도록 설정되어 있습니다.