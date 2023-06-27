import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'

import App from './App.vue'
import router from './router'
import axios from './axios'

createApp(App)
.use(createPinia())
.use(router)
.use(ElementPlus)
.provide("$axios", axios)
.provide("$router", router)
.mount('#app')