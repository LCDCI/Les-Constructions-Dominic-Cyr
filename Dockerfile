############################################################
# 1) Build FRONTEND (React)
############################################################
FROM node:20 AS frontend-build
WORKDIR /app

# Copy package.json and lock file
COPY frontend/les_constructions_dominic_cyr/package*.json ./

RUN npm install

# Copy the entire frontend code
COPY frontend/les_constructions_dominic_cyr ./

RUN npm run build


############################################################
# 2) Build BACKEND (Spring Boot)
############################################################
FROM gradle:8.5-jdk21 AS backend-build
WORKDIR /app

# Copy entire backend repository
COPY . .

# Copy frontend build output to Spring Boot static folder
COPY --from=frontend-build /app/build ./src/main/resources/static

RUN gradle clean build -x test --no-daemon


############################################################
# 3) Build FINAL RUNTIME IMAGE
############################################################
FROM eclipse-temurin:21-jdk
WORKDIR /app

EXPOSE 8080

COPY --from=backend-build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
