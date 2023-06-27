import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import ElementPlus from 'element-plus'

import App from './App.vue'
import router from './router'
import axios from './axios'


const app = createApp(App)

app.provide("$axios", axios)

app.use(createPinia())
.use(router)
.use(ElementPlus)
.mount('#app')