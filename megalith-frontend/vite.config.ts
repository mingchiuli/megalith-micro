import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());
  const fileName = env.VITE_APP_NAME

  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        resolvers: [ElementPlusResolver()],
      }),
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
      // 提高 chunk 大小警告限制（可选）
      chunkSizeWarningLimit: 1000,
      rollupOptions: {
        output: {
          // 手动分包策略
          manualChunks: {
            // Vue 核心库
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            
            // Element Plus 组件库
            'element-plus': ['element-plus'],
            
            // Markdown 编辑器相关（这是你最大的包）
            'md-editor': ['md-editor-v3', 'markdown-it'],
            
            // 代码高亮
            'highlight': ['highlight.js'],
            
            // 协同编辑相关
            'yjs-vendor': ['yjs', 'y-websocket', 'y-codemirror.next'],
            
            // 其他工具库
            'utils': ['axios', 'js-base64', '@vavt/v3-extension']
          },
          
          // 为每个 chunk 生成更小的文件
          chunkFileNames: (chunkInfo) => {
            const facadeModuleId = chunkInfo.facadeModuleId 
              ? chunkInfo.facadeModuleId.split('/').pop() 
              : 'chunk';
            return `assets/${facadeModuleId}/[name]-[hash].js`;
          }
        }
      }
    },
    
    // 优化依赖预构建
    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'element-plus',
        'axios'
      ]
    }
  }
})