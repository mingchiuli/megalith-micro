import { expect, test } from '@playwright/test'
import { login } from './session'

test('recommendation feed retries a dropped browser request through the gateway', async ({
  page
}) => {
  await page.goto('/')
  await page.route(
    '**/api/public/blog/recommendations*',
    (route) => route.abort('connectionfailed'),
    { times: 1 }
  )
  await page.getByRole('link', { name: /Content list|内容列表/i }).click()
  const failure = page.locator('.infinite-footer__error')
  await expect(failure).toBeVisible()
  const recovered = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/public/blog/recommendations')
  )
  await failure.locator('.el-link').click()
  expect((await recovered).ok()).toBeTruthy()
  await expect(failure).toHaveCount(0)
})

test('draft allocation can be retried after a network interruption', async ({ page }) => {
  await login(page)
  await page.route('**/api/sys/blog/draft/new', (route) => route.abort('connectionfailed'), {
    times: 1
  })
  await page.goto('/sys/edit')
  const retry = page.locator('.el-alert').getByRole('button', { name: /Reconnect|重新连接/i })
  await expect(retry).toBeVisible()
  const created = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/sys/blog/draft/new')
  )
  await retry.click()
  expect((await created).status()).toBe(200)
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  await expect(page.getByRole('button', { name: /^(Publish|发布)$/ })).toBeEnabled({
    timeout: 15000
  })
})

test('session expiry in an open admin page offers sign-in with the original destination', async ({
  page,
  context
}) => {
  await login(page, '/sys/users')
  await expect(page.locator('.el-table__body-wrapper tr').first()).toBeVisible()
  await context.clearCookies({ name: 'megalith_access_token' })
  await context.clearCookies({ name: 'megalith_refresh_token' })
  await page.getByRole('button', { name: '编辑', exact: true }).first().click()
  const expired = page.getByRole('dialog', { name: /Session expired|登录已过期|会话已过期/i })
  await expect(expired).toBeVisible()
  await expired.getByRole('button').click()
  await expect(page).toHaveURL(/\/login\?redirect=(%2F|\/)sys(%2F|\/)users/i)
})
