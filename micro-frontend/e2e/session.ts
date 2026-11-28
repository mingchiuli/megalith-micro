import { expect, type Page } from '@playwright/test'

export async function login(page: Page, next = '/backend') {
  const username = process.env.E2E_USERNAME
  const password = process.env.E2E_PASSWORD
  if (!username || !password)
    throw new Error('Set E2E_USERNAME and E2E_PASSWORD for the isolated fixture')
  await page.goto('/login?redirect=' + encodeURIComponent(next))
  const inputs = page.locator('input[placeholder]')
  await inputs.first().fill(username)
  await inputs.last().fill(password)
  const loggedIn = page.waitForResponse(
    (r) => r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/api/login')
  )
  await page.locator('.dialog-footer button:visible').click()
  expect((await loggedIn).status()).toBe(200)
  await expect(page).toHaveURL(new RegExp(next + '$'))
}
