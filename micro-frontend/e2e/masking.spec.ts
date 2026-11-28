import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'

test('published spans use UTF-16 offsets and the author and public reader see different titles', async ({
  page,
  browser
}) => {
  test.setTimeout(60000)
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const id = new URL(page.url()).searchParams.get('id')!
  const suffix = randomUUID().slice(0, 6)
  const title = '甲😀乙' + suffix
  const titleField = page.locator('.title input')
  await titleField.fill(title)
  await page.locator('.desc textarea').fill('UTF-16 fixture')
  const editor = page.locator('#md-editor .cm-content')
  await expect(editor).toHaveAttribute('contenteditable', 'true')
  await editor.click()
  await page.keyboard.insertText('A visible paragraph.')
  await page.locator('.status label:has(input[value="1"])').click()
  await page.locator('.status label:has(input[value="0"])').click()
  await page.locator('.status label:has(input[value="2"])').click()
  await titleField.focus()
  await titleField.evaluate((input: HTMLInputElement) => input.setSelectionRange(1, 1))
  await titleField.press('Shift+ArrowRight')
  await expect
    .poll(() =>
      titleField.evaluate((input: HTMLInputElement) => [input.selectionStart, input.selectionEnd])
    )
    .toEqual([1, 3])
  await expect(page.locator('.el-form-item .el-tag').filter({ hasText: '1' })).toBeVisible()
  const tag = page.locator('.el-form-item .el-tag').filter({ hasText: '1' })
  await titleField.press('ArrowRight')
  await tag.locator('.el-tag__close').click()
  await expect(tag).toHaveCount(0)
  await titleField.focus()
  await titleField.evaluate((input: HTMLInputElement) => input.setSelectionRange(1, 1))
  await titleField.press('Shift+ArrowRight')
  await expect(tag).toBeVisible()
  const committed = page.waitForResponse(
    (r) => r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/publish')
  )
  await page.getByRole('button', { name: /^(Publish|发布)$/ }).click()
  expect((await committed).ok()).toBeTruthy()
  const origin = new URL(page.url()).origin
  await page.goto('/blog/' + id, { waitUntil: 'domcontentloaded' })
  await expect(page.locator('.exhibit-title')).toHaveText(title)
  const guest = await browser.newContext({
    baseURL: origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1'
  })
  try {
    const reader = await guest.newPage()
    await reader.goto('/blog/' + id, { waitUntil: 'domcontentloaded' })
    await expect(reader.locator('.exhibit-title')).toHaveText('甲++乙' + suffix)
  } finally {
    await guest.close()
  }
})
