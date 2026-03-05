# Spring Boot 애플리케이션용 Dockerfile (멀티 스테이지 빌드)

# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk17 AS builder

# 작업 디렉터리 설정
WORKDIR /app

# Gradle 설정 파일들 먼저 복사 (캐시 최적화)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle/

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src ./src/

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jdk-jammy

# 시스템 패키지 업데이트 및 필요한 도구 설치
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 애플리케이션 실행을 위한 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring

# 작업 디렉터리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 로그 디렉터리 생성
RUN mkdir -p /app/logs && chown -R spring:spring /app

# 실행 사용자 변경
USER spring

# JVM 옵션 설정 (환경변수)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication"

# Spring 프로파일 설정
ENV SPRING_PROFILES_ACTIVE=dev

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
