/// <reference types="vite/client" />
// eslint-disable-next-line import/namespace
import { defineConfig } from 'vite';
import federation from '@originjs/vite-plugin-federation';
import { fileURLToPath, URL } from 'url';
import fs from 'fs';

const localTranslationsPlugin = () => {
  const virtualModuleId = 'virtual:local-translations';
  const resolvedVirtualModuleId = `\0${virtualModuleId}`;

  return {
    name: 'local-translations',
    resolveId(id) {
      return id === virtualModuleId ? resolvedVirtualModuleId : undefined;
    },
    load(id) {
      if (id !== resolvedVirtualModuleId) return undefined;

      const translationsDirectory = fileURLToPath(
        new URL('../../translation-scripts/translation-files', import.meta.url)
      );
      const translations = { en: {}, fr: {} };

      for (const fileName of fs.readdirSync(translationsDirectory)) {
        const match = fileName.match(/^(.+)_(en|fr)\.json$/i);
        if (!match) continue;

        const [, pageName, language] = match;
        const filePath = new URL(
          `../../translation-scripts/translation-files/${fileName}`,
          import.meta.url
        );
        translations[language.toLowerCase()][pageName.toLowerCase()] =
          JSON.parse(fs.readFileSync(fileURLToPath(filePath), 'utf8'));
      }

      return `export default ${JSON.stringify(translations)};`;
    },
  };
};

export default defineConfig(async () => {
  // eslint-disable-next-line import/no-unresolved
  const { default: reactPlugin } = await import('@vitejs/plugin-react');
  const BACKEND_PORT = 8080;

  return {
    plugins: [
      reactPlugin(),
      localTranslationsPlugin(),
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
      fs: {
        allow: [fileURLToPath(new URL('../../translation-scripts', import.meta.url))],
      },
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
      // Ensure every package (including react-big-schedule) reuses the same React instance
      dedupe: ['react', 'react-dom'],
    },
    build: {
      modulePreload: false,
      target: 'esnext',
      assetsDir: 'src/assets',
    },
  };
});
