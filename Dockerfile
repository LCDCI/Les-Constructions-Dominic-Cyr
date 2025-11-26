# ================================
# BACKEND - SPRING BOOT (Java 21)
# ================================
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Gradle & wrapper
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle

# Download Gradle deps
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build artifact
RUN ./gradlew clean build -x test --no-daemon

# ================================
# Runtime image
# ================================
FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
