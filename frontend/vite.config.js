import { fileURLToPath, URL } from 'node:url'
import { env } from 'node:process'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const backendTarget = env.VITE_BACKEND_URL || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue(), vueDevTools()],
  server: {
    proxy: {
      '/api': backendTarget,
      '/oauth2': backendTarget,
      '/login/oauth2': backendTarget,
      '/logout': backendTarget,
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
