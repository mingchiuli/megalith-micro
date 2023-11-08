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
          target: 'http://127.0.0.1:8081',	//接口地址
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/api/, '')
        },
        '/ws': {
          target: 'ws://127.0.0.1:8081',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/wsapi/, '')
        }
      }
    },
    build: {
      outDir: fileName,
      assetsPublicPath: './'
    }
  }
})

