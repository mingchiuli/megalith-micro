/// <reference types="vitest" />
import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  const fileName = env.VITE_APP_NAME

  return {
    plugins: [
      vue(),
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          'pinia',
          {
            vue: ['createApp'],
            'vue-router': ['createRouter', 'createWebHistory']
          }
        ],
        resolvers: [ElementPlusResolver()]
      }),
      Components({
        resolvers: [ElementPlusResolver()]
      })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      host: '127.0.0.1',
      port: 1919,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8088',
          rewrite: (path) => path.replace(/^\/api/, '')
        },
        '/wsapi': {
          target: 'ws://127.0.0.1:8088',
          ws: true,
          rewrite: (path) => path.replace(/^\/wsapi/, '')
        }
      }
    },
    build: {
      outDir: fileName,
      chunkSizeWarningLimit: 1000
    },

    // 优化依赖预构建
    optimizeDeps: {
      include: ['vue', 'vue-router', 'pinia', 'element-plus', 'axios']
    },

    // Vitest 单元测试配置
    test: {
      globals: true,
      environment: 'jsdom',
      include: ['src/**/__tests__/*.test.ts'],
      setupFiles: ['src/test/setup.ts'],
      // 让 Vite 内联处理 element-plus，避免 Node ESM 解析 .css 报错
      server: {
        deps: {
          inline: ['element-plus']
        }
      },
      coverage: {
        provider: 'v8',
        reporter: ['text', 'html'],
        include: ['src/**/*.{ts,vue}'],
        exclude: ['src/**/__tests__/**', 'src/main.ts', 'src/type/**']
      }
    }
  }
})
