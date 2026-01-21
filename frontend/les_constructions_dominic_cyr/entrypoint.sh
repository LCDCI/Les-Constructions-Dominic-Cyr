#!/bin/sh
# Just copy the config and start nginx (no templating needed for deployment)
cp /etc/nginx/conf.d/default.conf.template /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'