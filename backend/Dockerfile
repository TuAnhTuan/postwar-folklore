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

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
