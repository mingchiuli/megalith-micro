import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import App from './App.vue'
import router from './router'
import * as Icons from '@element-plus/icons-vue'
import { MdPreview, config } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { collaborationManager } from './config/collaborationManager'

const app = createApp(App).use(createPinia()).use(router).use(ElementPlus).use(MdPreview)

Object.keys(Icons).forEach((key) => app.component(key, Icons[key as keyof typeof Icons]))

// 在应用启动时配置 CodeMirror，使用协作管理器的扩展
config({
  codeMirrorExtensions(_theme, extensions) {
    // 添加协作扩展，但不连接服务器
    return [...extensions, collaborationManager.getExtension()]
  },
})

app.mount('#app')