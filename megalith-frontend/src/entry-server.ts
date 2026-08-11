import { renderToString, type SSRContext } from '@vue/server-renderer'
import { createHead, renderSSRHead } from '@unhead/vue/server'
import { stringify } from 'devalue'
import { createMegalithApp, type AppRequestContext } from './app'

export type RenderResult = {
  appHtml: string
  headTags: string
  htmlAttrs: string
  bodyAttrs: string
  bodyTags: string
  state: string
  status: number
  route: string
  redirect?: string
  modules: Set<string>
  setCookies: string[]
}

export const render = async (url: string, request: AppRequestContext): Promise<RenderResult> => {
  const head = createHead()
  const { app, router, pinia, responseCookies } = createMegalithApp({
    server: true,
    head,
    request
  })
  const requestedPath = new URL(url, 'http://ssr.local').pathname

  await router.push(url)
  await router.isReady()
  const context: SSRContext = {}
  const appHtml = await renderToString(app, context)
  const renderedHead = await renderSSRHead(head)
  const finalRoute = router.currentRoute.value
  const redirect = finalRoute.path !== requestedPath ? finalRoute.fullPath : undefined

  return {
    appHtml,
    headTags: renderedHead.headTags,
    htmlAttrs: renderedHead.htmlAttrs,
    bodyAttrs: renderedHead.bodyAttrs,
    bodyTags: renderedHead.bodyTags,
    state: stringify(pinia.state.value),
    status: redirect ? 302 : ((finalRoute.meta.status as number | undefined) ?? 200),
    route: finalRoute.matched.at(-1)?.path ?? finalRoute.path,
    redirect,
    modules: context.modules ?? new Set<string>(),
    setCookies: responseCookies
  }
}
