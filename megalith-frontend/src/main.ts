import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'

import App from './App.vue'
import router from './router'

import "element-plus/theme-chalk/el-message.css";
import "element-plus/theme-chalk/el-message-box.css"; 

createApp(App)
  .use(createPinia())
  .use(router)
  .use(ElementPlus)
  .mount('#app')