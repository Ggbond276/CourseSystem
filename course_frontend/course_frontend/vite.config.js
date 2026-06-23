import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 核心跨域代码
  server: {
    port: 5173, // 明确锁定前端运行的端口
    proxy: {
      // 这里的 '/api' 是一个暗号。
      // 意思是：以后前端发出的请求，只要是以 '/api' 开头的，统统走这座桥！
      '/api': {
        target: 'http://localhost:8888', // 桥的尽头：你的 Spring Boot 后端
        changeOrigin: true, // 开启欺骗模式，允许跨域
        // 如果你后端的接口路径里没有包含 "api" 这个词（比如直接叫 /course/list）
        // 就需要下面这行把暗号擦掉再发给后端；如果后端接口本来就带 /api，就把下面这行注释掉。
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
