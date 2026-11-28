import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'
import { findTableRow } from './table'

test('homepage, theme, empty search, clear search, back and forward', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('link', { name: 'GitHub' })).toHaveAttribute(
    'href',
    'https://github.com/mingchiuli'
  )
  await page.getByRole('link', { name: /Content list|内容列表/i }).click()
  await expect(page).toHaveURL(/\/blogs$/)
  const prior = await page.locator('html').evaluate((e) => e.classList.contains('dark'))
  await page.locator('.theme-button').click()
  await page.reload()
  expect(await page.locator('html').evaluate((e) => e.classList.contains('dark'))).toBe(!prior)
  await page.locator('.search-button').click()
  const dialog = page.getByRole('dialog')
  await dialog.locator('input').fill('miss' + randomUUID().slice(0, 6))
  await dialog.getByRole('button', { name: /Confirm|确定/ }).click()
  await expect(page.locator('.feed-empty')).toBeVisible()
  await page
    .locator('a.door')
    .filter({ hasText: /^(Back|返回)$/i })
    .click()
  await expect(page.locator('a.door').filter({ hasText: /^(Back|返回)$/i })).toHaveCount(0)
  await page.goBack()
  await expect(page.getByAltText('Megalith')).toBeVisible()
  await page.goForward()
  await expect(page).toHaveURL(/\/blogs$/)
})

test('invited visitor validates, registers, signs in and consumes the single-use link', async ({
  page,
  browser
}) => {
  await login(page, '/sys/users')
  const name = 'reg' + randomUUID().slice(0, 8)
  const address = new URL(page.url()).origin
  // The bound-invitation flow starts with an existing account.
  const created = await page.request.post(address + '/api/sys/user/save', {
    headers: { Origin: address },
    data: {
      username: name,
      nickname: name,
      password: 'InitialTest123',
      avatar: address + '/favicon.ico',
      email: name + '@example.org',
      phone: '139' + String(Math.floor(Math.random() * 1e8)).padStart(8, '0'),
      status: 0,
      roles: ['user']
    }
  })
  expect(created.ok()).toBeTruthy()
  await page.reload()
  const row = await findTableRow(page, name)
  const linkResponse = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/sys/user/auth/register/page')
  )
  await row.getByRole('button', { name: '更新链接', exact: true }).click()
  const { data } = (await (await linkResponse).json()) as { data: string }
  const link = new URL(data)
  const context = await browser.newContext({
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    baseURL: new URL(page.url()).origin,
    locale: 'en-US'
  })
  try {
    const guest = await context.newPage()
    await guest.goto(link.pathname + link.search)
    await expect(guest.locator('.username input')).toHaveValue(name)
    await expect(guest.locator('.username input')).toBeDisabled()
    await guest.locator('.nickname input').fill(name)
    const passwords = guest.locator('.password input')
    await passwords.first().fill('MegalithTest123')
    await passwords.last().fill('different')
    await guest.locator('.email input').fill(name + '@example.org')
    await guest
      .locator('.phone input')
      .fill('135' + String(Math.floor(Math.random() * 1e8)).padStart(8, '0'))
    await guest.locator('.submit-button button').click()
    await expect(guest.locator('.password .el-form-item__error')).toBeVisible()
    await passwords.last().fill('MegalithTest123')
    await guest.locator('.submit-button button').click()
    await expect(guest).toHaveURL(/\/backend$/)
    await expect(guest.locator('.header-dropdown')).toContainText(name)
    await guest.goto(link.pathname + link.search)
    await expect(guest).toHaveURL(/\/blogs$/)
    await guest.goto('/sys/users')
    await expect(guest.getByRole('button', { name: '新增', exact: true })).toHaveCount(0)
  } finally {
    await context.close()
    await row.getByRole('button', { name: '删除', exact: true }).click()
    await page.locator('.el-popconfirm:visible .el-button--primary').press('Enter')
  }
})

test('unknown email and SMS login codes show a failure and keep the form usable', async ({
  page
}) => {
  for (const [channel, name] of [
    ['EMAIL', 'e2e-no-recipient@example.invalid'],
    ['SMS', 'not-a-registered-phone']
  ]) {
    await page.goto('/login')
    await page.locator('label:has(input[value="' + channel + '"])').click()
    const inputs = page.locator('input[placeholder]')
    await inputs.first().fill(name)
    const send = page.waitForResponse((r) =>
      new URL(r.url()).pathname.endsWith('/code/' + channel.toLowerCase())
    )
    await page.locator('.dialog-footer button:visible').last().click()
    expect((await send).status()).toBe(404)
    await expect(page.locator('.dialog-footer button:visible').last()).toBeEnabled()
    await inputs.last().fill('invalid')
    const attempt = page.waitForResponse((r) => new URL(r.url()).pathname.endsWith('/api/login'))
    await page.locator('.dialog-footer button:visible').first().click()
    expect((await attempt).status()).toBe(401)
    await expect(page).toHaveURL(/\/login$/)
  }
})
