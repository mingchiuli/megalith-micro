import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { prepareArticles } from './articles'
import { login } from './session'

test('public recommendations continue from a cursor and the search and article filters can be cleared', async ({
  page,
  browser
}) => {
  test.setTimeout(90000)
  await login(page, '/sys/blogs')
  const prefix = 'filter' + randomUUID().slice(0, 8)
  await prepareArticles(page, prefix)
  const form = page.locator('.button-form')
  const keyword = form.locator('.search-input input')
  const response = () =>
    page.waitForResponse((r) => new URL(r.url()).pathname.endsWith('/sys/blog/blogs'))
  await keyword.fill(prefix)
  const filtered = response()
  await keyword.press('Enter')
  expect(new URL((await filtered).url()).searchParams.get('keywords')).toBe(prefix)
  const reset = response()
  await keyword.hover()
  await form.locator('.search-input .el-input__clear').click()
  expect(new URL((await reset).url()).searchParams.get('keywords')).toBeFalsy()
  const status = form.locator('.el-select')
  await status.click()
  await page.getByRole('option', { name: /^(Hidden|隐藏)$/i }).click()
  const cleared = response()
  await status.hover()
  await status.locator('.el-select__clear').click()
  expect(new URL((await cleared).url()).searchParams.get('status')).toBeFalsy()
  const guest = await browser.newContext({
    baseURL: new URL(page.url()).origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    locale: 'en-US'
  })
  try {
    await expect
      .poll(
        async () => {
          const res = await guest.request.get('/api/public/blog/recommendations')
          return ((await res.json()) as { data: { hasMore: boolean } }).data.hasMore
        },
        { timeout: 30000 }
      )
      .toBe(true)
    const reader = await guest.newPage()
    await reader.goto('/')
    const more = reader.waitForResponse((r) => {
      const url = new URL(r.url())
      return url.pathname.endsWith('/public/blog/recommendations') && url.searchParams.has('cursor')
    })
    await reader.getByRole('link', { name: /Content list|内容列表/i }).click()
    await reader.locator('.infinite-footer').scrollIntoViewIfNeeded()
    const feed = (await (await more).json()) as { data: { content: Array<{ title: string }> } }
    expect(feed.data.content.length).toBeGreaterThan(0)
    await expect(
      reader.locator('.el-card .title').filter({ hasText: feed.data.content[0]!.title }).first()
    ).toBeVisible()
    await reader.locator('.search-button').click()
    const dialog = reader.getByRole('dialog')
    const input = dialog.locator('input')
    await input.fill(prefix)
    await input.hover()
    await dialog.locator('.el-input__clear').click()
    await expect(input).toHaveValue('')
    await dialog.locator('.el-dialog__headerbtn').click()
    await expect(reader.locator('.el-card').first()).toBeVisible()
  } finally {
    await guest.close()
  }
})

test('users and roles accept an explicit page jump and a new route accepts a custom service port', async ({
  page
}) => {
  test.setTimeout(90000)
  await login(page)
  const origin = new URL(page.url()).origin
  const prefix = 'jump' + randomUUID().slice(0, 6)
  for (const resource of ['user', 'role']) {
    const list = resource === 'user' ? '/sys/user/page/1' : '/sys/role/roles?currentPage=1'
    const existing = await page.request.get(origin + '/api' + list, { headers: { Origin: origin } })
    expect(existing.ok()).toBeTruthy()
    const firstPage = (await existing.json()) as { data: { totalElements: number } }
    for (let i = firstPage.data.totalElements; i < 6; i++) {
      const name = prefix + resource + i
      const data =
        resource === 'user'
          ? {
              username: name,
              nickname: name,
              password: 'FixturePage123',
              roles: ['user'],
              status: 0,
              email: name + '@example.invalid',
              phone: '139' + String(Math.floor(Math.random() * 1e8)).padStart(8, '0'),
              avatar: origin + '/favicon.ico'
            }
          : { name, code: name, remark: name, status: 0 }
      expect(
        (
          await page.request.post(origin + '/api/sys/' + resource + '/save', {
            headers: { Origin: origin },
            data
          })
        ).ok()
      ).toBeTruthy()
    }
    await page.goto('/sys/' + resource + 's')
    const pages = page.locator('.el-pagination')
    await pages.locator('.el-pagination__sizes .el-select').click()
    await page.getByRole('option', { name: /^5\D/ }).click()
    const jump = pages.locator('.el-pagination__jump input')
    await jump.fill('2')
    await jump.press('Enter')
    await expect(pages.locator('.el-pager .is-active')).toHaveText('2')
    await expect(page.locator('.el-table__body-wrapper tr').first()).toBeVisible()
  }
  await page.goto('/sys/authorities')
  await page.getByRole('button', { name: '新增', exact: true }).click()
  const dialog = page.getByRole('dialog')
  const port = dialog.getByLabel(/^(Port|端口)$/i)
  await port.fill('18082')
  await expect(port).toHaveValue('18082')
  await dialog.locator('.el-dialog__headerbtn').click()
  await expect(dialog).toBeHidden()
})
