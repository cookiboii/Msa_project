# 빌드 단계
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
