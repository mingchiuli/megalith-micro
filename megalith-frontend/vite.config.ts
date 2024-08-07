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
      chunkSizeWarningLimit: 2000,
      cssCodeSplit: true, //css 拆分
      sourcemap: false, //不生成sourcemap
      minify: false, //是否禁用最小化混淆，esbuild打包速度最快，terser打包体积最小。
      assetsInlineLimit: 5000 //小于该值 图片将打包成Base64
    }
  }
})

