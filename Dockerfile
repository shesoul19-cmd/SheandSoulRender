# ---- Stage 1: Build the application ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn -B clean package -DskipTests

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Use an ARG so you can override if jar name differs
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
