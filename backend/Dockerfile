# ════════════════════════════════════════════════════════
# STAGE 1 — BUILD
# Dùng Maven image chứa sẵn JDK 21 để compile và package
# ════════════════════════════════════════════════════════
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml trước để Docker cache layer dependencies
# (layer này chỉ rebuild khi pom.xml thay đổi)
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ════════════════════════════════════════════════════════
# STAGE 2 — RUNTIME
# Dùng JRE slim (không cần full JDK khi chạy)
# ════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-jammy AS runtime

# Tạo user non-root để tăng security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

# Copy JAR từ stage builder
COPY --from=builder /app/target/*.jar app.jar

# Đổi owner
RUN chown appuser:appgroup app.jar

USER appuser

# Port mà Spring Boot lắng nghe
EXPOSE 8080

# JVM tuning cho container (tránh dùng hết RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
