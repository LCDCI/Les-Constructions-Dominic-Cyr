/// <reference types="vite/client" />
import { defineConfig } from 'vite'
import federation from '@originjs/vite-plugin-federation'
import { fileURLToPath, URL } from 'url'

export default defineConfig(async () => {
  // Use dynamic import so Vite/esbuild doesn't try to require() an ESM-only plugin
  // Change the package name below to the one you want to use:
  //  - '@vitejs/plugin-react'  OR
  //  - '@vitejs/plugin-react-swc'
  const { default: reactPlugin } = await import('@vitejs/plugin-react')

  return {
    plugins: [
      reactPlugin(),
      federation({
        name: 'les_constructions_dominic_cyr',
        shared: ['react', 'react-dom'],
      }),
    ],
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
  }
})
