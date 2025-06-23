# 1. 빌드 환경: Gradle과 JDK 17을 사용하여 .jar 파일을 생성
FROM gradle:7.6.1-jdk17-focal AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. 실행 환경: 빌드된 .jar 파일만 가져와서 최소한의 환경에서 실행
FROM openjdk:17-jdk-slim
WORKDIR /app
# 빌드 환경에서 생성된 .jar 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar
# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]