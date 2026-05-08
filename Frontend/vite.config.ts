import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    headers: {
      'Cache-Control': 'no-store',   // ← 캐시 완전 비활성화
    }
  }
})