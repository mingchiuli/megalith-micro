import './assets/main.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import { parse } from 'devalue'
import { createHead } from '@unhead/vue/client'
import type { StateTree } from 'pinia'
import { createMegalithApp } from './app'
import { themeStore } from './stores'

const stateElement = document.getElementById('__MEGALITH_STATE__')
const initialState = stateElement?.textContent
  ? (parse(stateElement.textContent) as StateTree)
  : undefined
const head = createHead()
const { app, router } = createMegalithApp({ server: false, head, initialState })

await router.isReady()
app.mount('#app')
themeStore().initTheme()
