# 1. 빌드 스테이지: 소스 코드를 JAR 파일로 빌드
# (베이스 이미지를 gradle 공식 이미지로 변경하여 안정성 확보)
FROM gradle:8.4-jdk17-focal AS build

WORKDIR /app

# [개선점 1] Gradle 의존성을 먼저 받아 별도의 레이어로 캐싱하여 빌드 속도 향상
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

# 의존성 변경이 없을 경우, 여기부터 빌드가 다시 시작됩니다.
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# -----------------------------------------------------

# 2. 실행 스테이지: 실제 운영 환경에서 사용될 이미지
# [개선점 2] JDK가 아닌 JRE(Java 실행 환경) 이미지를 사용하여 이미지 크기를 줄이고 보안 강화
FROM openjdk:17-jre-slim

WORKDIR /app

# [개선점 3] HEALTHCHECK에 필요한 curl 설치
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# [개선점 4] HEALTHCHECK 추가로 컨테이너 상태 모니터링
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# [개선점 5] 외부 application.properties를 사용하도록 ENTRYPOINT 수정
ENTRYPOINT ["java", "-Dspring.config.location=file:/app/config/application.properties", "-jar", "app.jar"]