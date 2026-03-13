import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

import tailwindcss from '@tailwindcss/vite'

function normalizeBasePath(value: string | undefined) {
  const raw = (value ?? '/').trim()
  if (!raw) return '/'
  const withLeading = raw.startsWith('/') ? raw : `/${raw}`
  return withLeading.endsWith('/') ? withLeading : `${withLeading}/`
}

// https://vite.dev/config/
export default defineConfig({
  base: normalizeBasePath(process.env.VITE_SITE_BASE_PATH),
  plugins: [
    vue(),
    tailwindcss(),
    vueDevTools(),
  ],
  server: {
    watch: {
      ignored: ['**/public/export/**'],
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
