import { describe, expect, it } from 'vitest'
import { renderSsrPage } from '../../server/ssr'

describe('SSR page template', () => {
  it('renders Vue teleports into the Element Plus popper container', async () => {
    const response = await renderSsrPage(
      {
        method: 'GET',
        url: 'https://chiu.wiki/sys/roles',
        headers: new Headers()
      },
      {
        loadTemplate: async () => `<!doctype html>
          <html lang="en"><head><!--app-head--></head><body>
          <div id="app"><!--app-html--></div>
          <!--app-state--><!--app-body-tags-->
          <div id="el-popper-container-0"><!--app-teleports--></div>
          </body></html>`,
        loadRender: async () => async () => ({
          appHtml: '<main>Roles</main>',
          headTags: '',
          htmlAttrs: 'lang="zh-CN"',
          bodyAttrs: '',
          bodyTags: '',
          teleports: {
            '#el-popper-container-0': '<!--teleport start anchor--><div>Options</div>'
          },
          state: '[]',
          status: 200,
          route: '/sys/roles',
          modules: new Set<string>(),
          setCookies: []
        }),
        ssrManifest: {}
      }
    )

    expect(response.body).toContain(
      '<div id="el-popper-container-0"><!--teleport start anchor--><div>Options</div></div>'
    )
    expect(response.body).not.toContain('<!--app-teleports-->')
  })
})
