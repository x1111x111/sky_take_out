import vue from '@vitejs/plugin-vue'
import { defineConfig, loadEnv } from 'vite'

function backendProxy(target) {
  return {
    target,
    changeOrigin: true,
    configure(proxy) {
      proxy.on('proxyReq', (proxyRequest, request) => {
        // Browser requests to this same-origin development proxy become
        // same-origin upstream requests. Preserve unrelated cross-site origins.
        const origin = request.headers.origin
        if (!origin) return
        try {
          if (new URL(origin).host === request.headers.host) {
            proxyRequest.setHeader('origin', new URL(target).origin)
          }
        } catch {
          // Leave malformed origins for the backend to reject.
        }
      })
    },
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.SKY_BACKEND_URL || 'http://localhost:8080'
  const proxy = {
    '/api': {
      ...backendProxy(target),
      rewrite: path => path.replace(/^\/api/, ''),
    },
    '/uploads': backendProxy(target),
  }
  return {
    plugins: [vue()],
    server: { host: '127.0.0.1', port: 5173, strictPort: true, proxy },
    preview: { host: '127.0.0.1', port: 4173, proxy },
  }
})
