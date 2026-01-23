############################################################
# Build Vite frontend
############################################################
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies (deterministic)
COPY package*.json ./
RUN npm ci

# Copy source code
COPY . .

# Build-time env vars (Vite reads at build time)
ARG VITE_AUTH0_DOMAIN
ARG VITE_AUTH0_CLIENT_ID
ARG VITE_AUTH0_AUDIENCE
ARG VITE_API_BASE=/api/v1

ENV VITE_AUTH0_DOMAIN=${VITE_AUTH0_DOMAIN}
ENV VITE_AUTH0_CLIENT_ID=${VITE_AUTH0_CLIENT_ID}
ENV VITE_AUTH0_AUDIENCE=${VITE_AUTH0_AUDIENCE}
ENV VITE_API_BASE=${VITE_API_BASE}

# Build
RUN npm run build


############################################################
# Serve with nginx
############################################################
FROM nginx:stable-alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Install envsubst and copy nginx template (will be rendered at container start)
RUN apk add --no-cache gettext
COPY --from=build /app/nginx/default.conf /etc/nginx/conf.d/default.conf.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
