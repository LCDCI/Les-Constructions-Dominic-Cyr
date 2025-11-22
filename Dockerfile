# Basic multi-stage Dockerfile for the Spring Boot (Gradle) application.
# Uses Eclipse Temurin images (well-maintained) for Java 21.
# Build stage creates the Spring Boot fat jar with the Gradle wrapper.
# Runtime stage runs the jar with a small JRE image.

# --- Build stage ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app

# copy gradle wrapper and config first to leverage layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# copy the rest of the project
COPY . .

# Ensure wrapper is executable and run the build
RUN chmod +x ./gradlew && \
    # build bootJar; skip tests for faster image builds (remove -x test to run tests)
    ./gradlew clean bootJar -x test --no-daemon

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar produced by spring boot (build/libs/*.jar)
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Expose the default port used by Spring Boot (change if necessary)
EXPOSE 8080

# Default JVM options (can be overridden via environment variable)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]