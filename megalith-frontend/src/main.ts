import './assets/main.css'
import App from './App.vue'
import router from './router'
// 导入 Element Plus 暗黑模式样式
import 'element-plus/theme-chalk/dark/css-vars.css'
import { themeStore } from './stores'
import { i18n } from './i18n'

const pinia = createPinia()
const app = createApp(App).use(pinia).use(router).use(i18n)

app.mount('#app')

// 初始化主题
const theme = themeStore()
theme.initTheme()
