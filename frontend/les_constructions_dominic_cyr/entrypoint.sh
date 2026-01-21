#!/bin/sh
# Substitute BACKEND_URL in nginx config template and start nginx
envsubst '$BACKEND_URL' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'