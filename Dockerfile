



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
# 2) FRONTEND BUILD (Node)
############################################################
FROM node:20 AS frontend-build
WORKDIR /frontend

# Copy frontend source
COPY frontend/les_constructions_dominic_cyr ./

# Install dependencies and build
RUN npm install && npm run build

############################################################
# 3) FINAL RUNTIME IMAGE (nginx + backend)
############################################################
FROM eclipse-temurin:21-jdk AS final
WORKDIR /app

# Copy backend JAR
COPY --from=backend-build /app/build/libs/les_constructions_dominic_cyr-0.0.1-SNAPSHOT.jar /app/app.jar

# Install nginx
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

# Copy built frontend and nginx config
COPY --from=frontend-build /frontend/dist/ /usr/share/nginx/html/
COPY frontend/les_constructions_dominic_cyr/nginx/default.conf /etc/nginx/conf.d/default.conf

# Expose ports
EXPOSE 8080 80

# Start both backend and nginx
CMD service nginx start && java -jar /app/app.jar