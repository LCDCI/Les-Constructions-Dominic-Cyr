/// <reference types="vite/client" />
import { defineConfig } from 'vite';
import federation from '@originjs/vite-plugin-federation';
import { fileURLToPath, URL } from 'url';

export default defineConfig(async () => {
  const { default: reactPlugin } = await import('@vitejs/plugin-react');
  const BACKEND_PORT = 8080;

  return {
    plugins: [
      reactPlugin(),
      federation({
        name: 'les_constructions_dominic_cyr',
        shared: ['react', 'react-dom'],
      }),
      {
        name: 'disable-cache',
        configureServer(server) {
          server.middlewares.use((req, res, next) => {
            if (
              req.url?.endsWith('.html') ||
              req.url?.endsWith('.js') ||
              req.url?.endsWith('.jsx')
            ) {
              res.setHeader(
                'Cache-Control',
                'no-store, no-cache, must-revalidate, proxy-revalidate'
              );
              res.setHeader('Pragma', 'no-cache');
              res.setHeader('Expires', '0');
            }
            next();
          });
        },
      },
    ],
    server: {
      proxy: {
        '/api/v1': {
          target: `http://localhost:${BACKEND_PORT}`,
          changeOrigin: true,
          secure: false,
          ws: true,
        },
      },
    },
    optimizeDeps: {
      force: true,
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
      dedupe: ['react', 'react-dom'],
    },
    build: {
      modulePreload: false,
      target: 'esnext',
      assetsDir: 'src/assets',
    },
  };
});