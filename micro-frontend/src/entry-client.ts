import './assets/main.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import { parse } from 'devalue'
import { createHead } from '@unhead/vue/client'
import type { StateTree } from 'pinia'
import { createMegalithApp } from './app'
import { resolveAppLocale } from './i18n'
import { themeStore } from './stores'

const stateElement = document.getElementById('__MEGALITH_STATE__')
const initialState = stateElement?.textContent
  ? (parse(stateElement.textContent) as StateTree)
  : undefined
const head = createHead()
const locale = resolveAppLocale(document.documentElement.lang, navigator.language)
const { app, router, pinia } = createMegalithApp({ server: false, head, initialState, locale })

await router.isReady()
app.mount('#app')
themeStore(pinia).initTheme()
