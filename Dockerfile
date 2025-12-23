# 1. 빌드 스테이지: 표준 JDK 환경에서 프로젝트의 Gradle Wrapper를 사용해 빌드
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Gradle 관련 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 분리 캐싱
RUN ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

# 소스 코드 복사
COPY src src

# 최종 JAR 파일 빌드
RUN ./gradlew build -x test

# -----------------------------------------------------

# 2. 실행 스테이지: JRE만 포함된 경량 이미지 사용
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# HEALTHCHECK에 필요한 curl 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너 상태 모니터링
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 외부 설정 파일을 참조하여 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
