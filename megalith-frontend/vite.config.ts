/// <reference types="vitest" />
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      ignore: ['createApp'],
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
    chunkSizeWarningLimit: 1000
  },

  ssr: {
    // Element Plus component entrypoints import CSS, which Node cannot load when externalized.
    noExternal: ['element-plus']
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
      provider: 'istanbul',
      reporter: ['text', 'html'],
      include: ['src/**/*.{ts,vue}'],
      exclude: ['src/**/__tests__/**', 'src/type/**'],
      thresholds: {
        'src/http/http.ts': { lines: 80, statements: 80, functions: 75, branches: 70 },
        'src/stores/{ssrStore,protectedBlogStore}.ts': {
          lines: 80,
          statements: 80,
          functions: 75,
          branches: 70
        },
        'src/composables/{useUniversalData,useLatestRequest}.ts': {
          lines: 80,
          statements: 80,
          functions: 75,
          branches: 70
        },
        'src/components/SearchItem.vue': {
          lines: 80,
          statements: 80,
          functions: 75,
          branches: 70
        }
      }
    }
  }
})
