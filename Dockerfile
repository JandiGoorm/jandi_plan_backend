# ==========================
# 1) Build Stage
#  - Gradle 컨테이너에서 빌드
# ==========================
FROM gradle:7.6-jdk17 AS builder

# 작업 디렉토리 생성/이동
WORKDIR /app

# Gradle 캐시 최적화를 위해 gradle 관련 파일만 먼저 복사하는 방식도 가능하나,
# 여기서는 단순화를 위해 전체 복사
COPY . .

# Spring Boot 애플리케이션 JAR 빌드
RUN gradle clean bootJar

# ==========================
# 2) Run Stage
#  - 최소 런타임 이미지(JRE)에서 실행
# ==========================
FROM eclipse-temurin:17-jre

# 애플리케이션 동작 디렉토리
WORKDIR /app

# 빌드 단계에서 만들어진 JAR만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너에서 오픈할 포트 (Cloud Run 등 사용 시 $PORT로 오버라이드 가능)
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]

# 빌드 스테이지에서는 프로젝트를 컴파일하고 실행 가능한 JAR 파일을 생성하고, 런 스테이지에서는 그 JAR 파일만 복사해서 실행
# 이렇게 하면 빌드 도구나 소스 코드 등 불필요한 파일들이 최종 런타임 이미지에 포함되지 않아서 이미지 크기를 최소화할 수 있다.