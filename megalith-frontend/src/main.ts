import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import * as Icons from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

const app = createApp(App).use(createPinia()).use(router).use(MdPreview)

Object.keys(Icons).forEach((key) => app.component(key, Icons[key as keyof typeof Icons]))

app.mount('#app')