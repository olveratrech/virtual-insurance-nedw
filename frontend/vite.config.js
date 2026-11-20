import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': 'http://localhost:8080',
      '/catalogos': 'http://localhost:8080',
      '/clientes': 'http://localhost:8080',
    },
  },
})
