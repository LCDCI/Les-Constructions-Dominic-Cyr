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
    ],
    server: {
      proxy: {
        '/api/v1': {
          target: `http://localhost:${BACKEND_PORT}`,
          changeOrigin: true,
          secure: false,
        },
      },
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    envDir: 'src/environments',
    build: {
      modulePreload: false,
      target: 'esnext',
      assetsDir: 'src/assets',
    },
  };
});
