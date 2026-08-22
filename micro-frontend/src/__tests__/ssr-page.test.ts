import { describe, expect, it } from 'vitest'
import { renderSsrPage } from '../../server/ssr'

describe('SSR page template', () => {
  it('renders body and Element Plus teleports into their target containers', async () => {
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
          <!--app-teleports-->
          </body></html>`,
        loadRender: async () => async () => ({
          appHtml: '<main>Roles</main>',
          headTags: '',
          htmlAttrs: 'lang="zh-CN"',
          bodyAttrs: '',
          bodyTags: '',
          teleports: {
            body: '<aside>Body teleport</aside>',
            '#el-popper-container-0': '<!--teleport start anchor--><div>Options</div>',
            '#el-popper-container-7': '<!--teleport start anchor--><div>More options</div>'
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

    expect(response.body).toContain('<aside>Body teleport</aside>')
    expect(response.body).toContain(
      '<div id="el-popper-container-0"><!--teleport start anchor--><div>Options</div></div>'
    )
    expect(response.body).toContain(
      '<div id="el-popper-container-7"><!--teleport start anchor--><div>More options</div></div>'
    )
    expect(response.body).not.toContain('<!--app-teleports-->')
  })
})
