import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'

test('history displays and compares two published revisions and restores the earlier public content', async ({
  page,
  browser
}) => {
  test.setTimeout(90000)
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const editUrl = page.url()
  const id = new URL(editUrl).searchParams.get('id')!
  const title = 'history-' + randomUUID().slice(0, 8)
  const editor = page.locator('#md-editor .cm-content')
  for (const text of ['First edition', 'Second edition']) {
    await expect(editor).toHaveAttribute('contenteditable', 'true')
    await page.locator('.title input').fill(title)
    await page.locator('.desc textarea').fill('Revision history fixture')
    await expect(page.getByRole('button', { name: /^(Publish|发布)$/ })).toBeEnabled()
    await editor.click()
    await editor.press('ControlOrMeta+a')
    await page.keyboard.insertText(text)
    await expect(editor).toHaveText(text)
    const commit = page.waitForResponse(
      (r) => new URL(r.url()).pathname.endsWith('/publish') && r.request().method() === 'POST'
    )
    await page.getByRole('button', { name: /^(Publish|发布)$/ }).click()
    expect((await commit).ok()).toBeTruthy()
    await expect(page).toHaveURL(/\/sys\/blogs$/)
    if (text === 'First edition') await page.goto(editUrl)
  }

  const row = page.getByRole('row').filter({ hasText: title })
  await expect(async () => {
    await page.reload()
    await expect(row).toContainText('Second edition', { timeout: 1200 })
  }).toPass({ timeout: 15000 })
  await row.getByRole('button', { name: /^(Version history|版本历史)$/ }).click()
  const history = page.locator('.el-drawer:visible')
  const previous = history.getByRole('row').filter({
    has: page.getByRole('button', { name: /Restore this published version|恢复此发布版本/ })
  })
  await expect(async () => {
    await history.getByRole('button', { name: /^(Search|搜索)$/ }).click()
    await expect(previous).toHaveCount(1, { timeout: 1200 })
  }).toPass({ timeout: 15000 })
  const detail = page.waitForResponse(
    (r) => /\/versions\/\d+$/.test(new URL(r.url()).pathname) && r.request().method() === 'GET'
  )
  await previous.getByRole('button', { name: /^(View|查看)$/ }).click()
  expect((await detail).ok()).toBeTruthy()
  const version = page.locator('.el-dialog:visible')
  await expect(version.locator('.version-content')).toContainText('First edition')
  await version.locator('.el-dialog__headerbtn').click()
  await history
    .getByRole('button', { name: /Compare with previous|与上一版本对比/ })
    .first()
    .click()
  const diff = page.locator('.el-dialog:visible')
  await expect(diff).toContainText('Second edition')
  await diff.locator('label:has(input[value="unified"])').click()
  await expect(diff.getByRole('radio', { name: /Unified diff|统一/ })).toBeChecked()
  await expect(diff).toContainText('First edition')
  await diff.locator('.el-dialog__headerbtn').click()

  await previous
    .getByRole('button', { name: /Restore this published version|恢复此发布版本/ })
    .click()
  const committed = page.waitForResponse(
    (r) => new URL(r.url()).pathname.endsWith('/publish') && r.request().method() === 'POST'
  )
  await page.locator('.el-message-box:visible .el-button--primary').click()
  expect((await committed).ok()).toBeTruthy()
  const guest = await browser.newContext({
    baseURL: new URL(editUrl).origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1'
  })
  try {
    const reader = await guest.newPage()
    await reader.goto('/blog/' + id, { waitUntil: 'domcontentloaded' })
    await expect(reader.locator('.content')).toContainText('First edition')
    await expect(reader.locator('.content')).not.toContainText('Second edition')
  } finally {
    await guest.close()
  }
})
