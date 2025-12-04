############################################################
# Build Vite frontend
############################################################
FROM node:20 AS build
WORKDIR /app

# copy package files for deterministic install
COPY package*.json ./
RUN npm ci --silent

# copy source and build
COPY . .

# If index.html is in public/index.html, copy it to project root so Vite can find it
RUN if [ -f public/index.html ] && [ ! -f index.html ]; then cp public/index.html index.html; fi

# ensure index.html is accessible (if you keep it in public, Vite will copy it; but this is the app root)
ARG VITE_API_BASE=/api/v1
ENV VITE_API_BASE=${VITE_API_BASE}
RUN npm run build

############################################################
# Serve with nginx
############################################################
FROM nginx:stable-alpine AS runtime
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]