import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import * as Icons from '@element-plus/icons-vue'

const app = createApp(App).use(createPinia()).use(router)

Object.keys(Icons).forEach((key) => app.component(key, Icons[key as keyof typeof Icons]))

app.mount('#app')