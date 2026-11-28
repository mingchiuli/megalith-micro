import { expect, test } from '@playwright/test'

test('the login form switches credentials across all three channels', async ({ page }) => {
  const response = await page.goto('/login')
  expect(response?.status()).toBe(200)
  const credential = page.locator('input[placeholder]').last()
  await credential.fill('previous credential')
  for (const channel of ['EMAIL', 'SMS', 'PASSWORD']) {
    await page.locator(`label:has(input[value="${channel}"])`).click()
    await expect(credential).toHaveValue('')
    await expect(page.locator('.dialog-footer button:visible')).toHaveCount(
      channel === 'PASSWORD' ? 1 : 2
    )
    await credential.fill('new credential')
  }
})

test('language survives a full document reload', async ({ page }) => {
  await page.goto('/login')
  const language = page.getByRole('combobox', { includeHidden: false })
  await language.selectOption('zh-CN')
  await page.reload()
  await expect(language).toHaveValue('zh-CN')
  await expect(page.locator('html')).toHaveAttribute('lang', 'zh-CN')
  await language.selectOption('en-US')
  await page.reload()
  await expect(page.locator('html')).toHaveAttribute('lang', 'en-US')
})

test('an unknown URL renders the 404 page on a direct SSR request', async ({ page }) => {
  const response = await page.goto('/e2e-unknown-page')
  expect(response?.status()).toBe(404)
  await expect(page.getByText('404 NOT FOUND')).toBeVisible()
  await expect(page.locator('.el-calendar')).toBeVisible()
})
