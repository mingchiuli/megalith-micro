import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import * as Icons from '@element-plus/icons-vue'
// 导入 Element Plus 暗黑模式样式
import 'element-plus/theme-chalk/dark/css-vars.css'

const app = createApp(App).use(createPinia()).use(router)

Object.keys(Icons).forEach((key) => app.component(key, Icons[key as keyof typeof Icons]))

app.mount('#app')