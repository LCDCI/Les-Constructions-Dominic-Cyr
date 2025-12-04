############################################################
# 1) BACKEND BUILD (Spring Boot)
############################################################
FROM gradle:8.5-jdk21 AS backend-build
WORKDIR /app

# Copy entire project (so Gradle sees build.gradle + src/)
COPY . .

# Build Spring Boot JAR
RUN gradle clean build -x test --no-daemon


############################################################
# 2) FINAL RUNTIME IMAGE
############################################################
FROM eclipse-temurin:21-jdk
WORKDIR /app

EXPOSE 8080

COPY --from=backend-build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]