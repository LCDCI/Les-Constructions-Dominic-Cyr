#!/bin/sh
set -e

# Set default BACKEND_API_URL if not provided
export BACKEND_API_URL=${BACKEND_API_URL:-http://localhost:8080}

# Replace env variables in nginx config
envsubst '$BACKEND_API_URL' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# Start nginx
exec nginx -g 'daemon off;'
