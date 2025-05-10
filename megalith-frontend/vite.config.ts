import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

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
          target: 'http://127.0.0.1:8081',
          rewrite: (path: string) => path.replace(/api/, '')
        },
        '/wsapi': {
          target: 'ws://127.0.0.1:8081',
          ws: true,
          rewrite: (path: string) => path.replace(/wsapi/, '')
        }
      }
    },
    build: {
      outDir: fileName,
      chunkSizeWarningLimit: 800, // Increased to reduce warnings while we optimize
      rollupOptions: {
        output: {
          manualChunks(id) {
            // Vue core libraries
            if (id.includes('node_modules/vue/') || 
                id.includes('node_modules/vue-router/') || 
                id.includes('node_modules/pinia/')) {
              return 'vue-core'
            }
            
            // Element Plus UI framework - split into smaller chunks
            if (id.includes('node_modules/element-plus/')) {
              if (id.includes('/lib/components/')) {
                // Group similar components together
                if (id.includes('/form/')) return 'el-form'
                if (id.includes('/table/')) return 'el-table'
                if (id.includes('/dialog/')) return 'el-dialog'
                if (id.includes('/button/')) return 'el-button'
                return 'el-components'
              }
              if (id.includes('/lib/theme-chalk/')) return 'el-theme'
              return 'element-plus-core'
            }
            
            // Editor related dependencies
            if (id.includes('@codemirror/') || id.includes('codemirror')) {
              if (id.includes('@codemirror/lang-')) {
                // Group similar languages together
                if (id.includes('javascript') || id.includes('typescript')) return 'editor-lang-js'
                if (id.includes('html') || id.includes('xml')) return 'editor-lang-markup'
                if (id.includes('css') || id.includes('less') || id.includes('scss')) return 'editor-lang-style'
                return 'editor-lang-other'
              }
              if (id.includes('@codemirror/view')) return 'editor-view'
              if (id.includes('@codemirror/state')) return 'editor-state'
              if (id.includes('@codemirror/commands')) return 'editor-commands'
              return 'editor-core'
            }
            
            // Markdown and KaTeX related
            if (id.includes('markdown-it') || id.includes('katex') || id.includes('highlight.js')) {
              if (id.includes('katex')) return 'markdown-katex'
              if (id.includes('highlight.js')) return 'markdown-highlight'
              return 'markdown-core'
            }
            
            // Utils and helpers
            if (id.includes('lodash-es') || id.includes('axios') || id.includes('dayjs')) {
              if (id.includes('lodash-es')) return 'utils-lodash'
              if (id.includes('axios')) return 'utils-axios'
              if (id.includes('dayjs')) return 'utils-date'
              return 'utils'
            }

            // Views
            if (id.includes('/src/views/')) {
              if (id.includes('/sys/')) return 'views-sys'
              return 'views-main'
            }
          }
        }
      }
    }
  }
})
