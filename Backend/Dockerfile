# ────────────────────────────────────────
# Stage 1 — Build
# ────────────────────────────────────────
FROM gradle:8.7-jdk17 AS build

WORKDIR /app

# 의존성 캐시 레이어 (소스 변경 시 재다운로드 방지)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 복사 후 빌드
COPY src ./src
RUN gradle clean bootJar --no-daemon -x test

# ────────────────────────────────────────
# Stage 2 — Run
# ────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 보안상 root 대신 별도 유저로 실행
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 빌드 결과물만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown appuser:appgroup app.jar

USER appuser

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "app.jar"]
