# 빌드 단계
FROM openjdk:17-jdk-alpine
WORKDIR /app

COPY build/libs/*.jar REDnotice.jar

# 컨테이너 시작 시 실행할 명령어
ENTRYPOINT ["java", "-jar", "REDnotice.jar"]

