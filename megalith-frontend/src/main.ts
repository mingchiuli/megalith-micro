import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import * as Icons from '@element-plus/icons-vue'
// 导入 Element Plus 暗黑模式样式
import 'element-plus/theme-chalk/dark/css-vars.css'
import { themeStore } from './stores/store'

const pinia = createPinia()
const app = createApp(App).use(pinia).use(router)

Object.keys(Icons).forEach((key) => app.component(key, Icons[key as keyof typeof Icons]))

app.mount('#app')

// 初始化主题
const theme = themeStore()
theme.initTheme()