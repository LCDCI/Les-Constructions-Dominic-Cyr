/// <reference types="vite/client" />
// eslint-disable-next-line import/namespace
import { defineConfig } from 'vite';
import federation from '@originjs/vite-plugin-federation';
import { fileURLToPath, URL } from 'url';

export default defineConfig(async () => {
  // eslint-disable-next-line import/no-unresolved
  const { default: reactPlugin } = await import('@vitejs/plugin-react');
  const BACKEND_PORT = 8080;

  return {
    plugins: [
      reactPlugin(),
      federation({
        name: 'les_constructions_dominic_cyr',
        shared: ['react', 'react-dom'],
      }),
      // Custom plugin to disable caching in development
      {
        name: 'disable-cache',
        configureServer(server) {
          server.middlewares.use((req, res, next) => {
            // Disable caching for HTML and JS files in development
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
    // Disable caching in development
    optimizeDeps: {
      force: true,
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    build: {
      modulePreload: false,
      target: 'esnext',
      assetsDir: 'src/assets',
    },
  };
});
