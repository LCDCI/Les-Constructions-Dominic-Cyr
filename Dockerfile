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

# Copy only the runnable Spring Boot JAR
COPY --from=backend-build /app/build/libs/les_constructions_dominic_cyr-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]