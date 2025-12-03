############################################################
# 1) FRONTEND BUILD (React)
############################################################
FROM node:20 AS frontend-build
WORKDIR /app

# Copy only package.json files first
COPY frontend/les_constructions_dominic_cyr/package*.json ./

RUN npm install

# Copy full frontend code
COPY frontend/les_constructions_dominic_cyr ./

# Build React app
RUN npm run build


############################################################
# 2) BACKEND BUILD (Spring Boot)
############################################################
FROM gradle:8.5-jdk21 AS backend-build
WORKDIR /app

# Copy entire project (so Gradle sees build.gradle + src/)
COPY . .

# Copy frontend build into Spring Boot resources/static
COPY --from=frontend-build /app/build ./src/main/resources/static

# Build Spring Boot JAR
RUN gradle clean build -x test --no-daemon


############################################################
# 3) FINAL RUNTIME IMAGE
############################################################
FROM eclipse-temurin:21-jdk
WORKDIR /app

EXPOSE 8080

COPY --from=backend-build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]