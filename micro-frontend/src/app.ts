import { createSSRApp, type App as VueApp } from 'vue'
import { createPinia, type Pinia, type StateTree } from 'pinia'
import type { VueHeadClient } from '@unhead/vue'
import { ElNotification, ID_INJECTION_KEY, ZINDEX_INJECTION_KEY } from 'element-plus'
import App from './App.vue'
import { createAppRouter, clearAuthStores } from '@/router'
import { createAppI18n, resolveAppLocale, type AppLocale } from '@/i18n'
import { createHttpClients } from '@/http/axios'
import { API_CLIENT_KEY, createApiClient, type ApiClient } from '@/http/http'
import { loginStateStore, themeStore } from '@/stores'

export type AppRequestContext = {
  cookie?: string
  acceptLanguage?: string
  origin?: string
  apiBaseURL?: string
}

export type CreateMegalithAppOptions = {
  server: boolean
  head: VueHeadClient
  initialState?: StateTree
  locale?: AppLocale
  request?: AppRequestContext
}

type MegalithApp = {
  app: VueApp
  pinia: Pinia
  router: ReturnType<typeof createAppRouter>
  api: ApiClient
  responseCookies: string[]
}

const parseCookies = (header = ''): Record<string, string> =>
  Object.fromEntries(
    header
      .split(';')
      .map((value) => value.trim().split('='))
      .filter((parts) => parts.length === 2)
      .map(([key, value]) => [key!, decodeURIComponent(value!)])
  )

const resolveLocale = (cookies: Record<string, string>, acceptLanguage = ''): AppLocale => {
  return resolveAppLocale(cookies.megalith_locale, acceptLanguage)
}

export const createMegalithApp = (options: CreateMegalithAppOptions): MegalithApp => {
  const responseCookies: string[] = []
  const cookies = parseCookies(options.request?.cookie)
  const pinia = createPinia()
  if (options.initialState) pinia.state.value = options.initialState

  const appContext: { router?: ReturnType<typeof createAppRouter> } = {}
  const clients = createHttpClients({
    baseURL: options.request?.apiBaseURL,
    cookie: options.request?.cookie,
    origin: options.request?.origin,
    onSetCookie: (values) => responseCookies.push(...values),
    onUnauthorized: () => {
      if (!appContext.router) return
      const loginState = loginStateStore(pinia)
      if (loginState.sessionExpired) return

      const currentRoute = appContext.router.currentRoute.value
      const redirect =
        currentRoute.path.startsWith('/sys') || currentRoute.path.startsWith('/backend')
          ? currentRoute.fullPath
          : undefined
      clearAuthStores(appContext.router, pinia)
      if (!options.server) loginState.markSessionExpired(redirect)
    },
    onError: (error) => {
      if (!options.server) {
        ElNotification.error({
          title: error.code,
          message: error.response?.data.msg ?? error.message,
          showClose: true
        })
      }
    }
  })
  const api = createApiClient(clients)
  const router = createAppRouter({ server: options.server, api, pinia })
  appContext.router = router
  const i18n = createAppI18n(
    options.locale ?? resolveLocale(cookies, options.request?.acceptLanguage)
  )
  const app = createSSRApp(App)

  app.use(pinia).use(router).use(i18n).use(options.head)
  app.provide(API_CLIENT_KEY, api)
  app.provide(ID_INJECTION_KEY, { prefix: 0, current: 0 })
  app.provide(ZINDEX_INJECTION_KEY, { current: 0 })

  if (!options.initialState) {
    loginStateStore(pinia).login = Boolean(
      cookies.megalith_access_token || cookies.megalith_refresh_token
    )
    themeStore(pinia).isDark = cookies.megalith_theme === 'dark'
  }

  return { app, pinia, router, api, responseCookies }
}
