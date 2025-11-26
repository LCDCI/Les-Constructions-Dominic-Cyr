# ================================
# FRONTEND - REACT (Node 20)
# ================================
FROM node:20 AS build

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy React source
COPY . .

# Build production assets
RUN npm run build

# ================================
# NGINX runtime
# ================================
FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
