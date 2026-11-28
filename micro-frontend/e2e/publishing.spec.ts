import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'
import { findDeletedArticle } from './recycle-bin'

test('two editors synchronize, save a version, publish, browse, take offline and restore', async ({
  page,
  browser
}) => {
  test.setTimeout(100000)
  const title = 'story-' + randomUUID().slice(0, 8)
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const editorUrl = page.url()
  const id = new URL(editorUrl).searchParams.get('id')!
  const titleField = page.locator('.title input')
  const summary = page.locator('.desc :is(input,textarea)')
  const editor = page.locator('#md-editor .cm-content')
  await expect(editor).toHaveAttribute('contenteditable', 'true', { timeout: 15000 })
  await titleField.fill(title)
  await summary.fill(title + ' summary')
  await editor.click()
  await page.keyboard.insertText('## chapter one')
  const second = await browser.newContext({
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    baseURL: new URL(editorUrl).origin,
    locale: 'en-US'
  })
  try {
    const peer = await second.newPage()
    await login(peer)
    await peer.goto(editorUrl)
    const other = peer.locator('#md-editor .cm-content')
    await expect(other).toContainText('chapter one')
    await other.press('ControlOrMeta+End')
    await peer.keyboard.insertText(' and two')
    await expect(editor).toContainText('and two')
  } finally {
    await second.close()
  }
  // Both editors autosave the draft. Reopen the persisted revision after the peer disconnects.
  await page.reload()
  await expect(editor).toContainText('chapter one and two')
  const version = page.waitForResponse(
    (r) => r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/versions')
  )
  await page.getByRole('button', { name: '保存版本', exact: true }).click()
  expect((await version).ok()).toBeTruthy()
  await page.reload()
  await expect(titleField).toHaveValue(title)
  await expect(editor).toContainText('chapter one and two')
  const publish = page.waitForResponse(
    (r) => r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/publish')
  )
  await page.getByRole('button', { name: /^(Publish|发布)$/ }).click()
  expect((await publish).ok()).toBeTruthy()
  await expect(page).toHaveURL(/\/sys\/blogs$/)
  const row = page.getByRole('row').filter({ hasText: title })
  await expect(async () => {
    await page.reload()
    await expect(row).toBeVisible({ timeout: 1200 })
  }).toPass({ timeout: 15000 })
  await row.getByRole('button', { name: /^(Version history|版本历史)$/ }).click()
  await expect(page.getByRole('dialog').getByRole('row').nth(1)).toBeVisible()
  await page
    .getByRole('dialog')
    .getByRole('button', { name: /Close this dialog|关闭/i })
    .first()
    .click()
  const guest = await browser.newContext({
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    baseURL: new URL(editorUrl).origin
  })
  try {
    const reader = await guest.newPage()
    await reader.goto('/blog/' + id, { waitUntil: 'domcontentloaded' })
    await expect(reader.locator('.exhibit-title')).toHaveText(title)
    await expect(reader.locator('.content')).toContainText('chapter one and two')
    await reader.reload({ waitUntil: 'domcontentloaded' })
    await expect(reader.locator('.exhibit-title')).toHaveText(title)
    const offline = page.waitForResponse(
      (r) => r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/offline')
    )
    await row.getByRole('button', { name: /^(Take offline|下线)$/i }).click()
    expect((await offline).ok()).toBeTruthy()
    await expect(async () => {
      await page.reload()
      await expect(row.getByRole('button', { name: /^(Bring online|重新上线)$/i })).toBeVisible({
        timeout: 1200
      })
    }).toPass({ timeout: 15000 })
    const online = page.waitForResponse(
      (r) => r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/online')
    )
    await row.getByRole('button', { name: /^(Bring online|重新上线)$/i }).click()
    expect((await online).ok()).toBeTruthy()
    await expect(async () => {
      await page.reload()
      await expect(row.getByRole('button', { name: /^(Take offline|下线)$/i })).toBeVisible({
        timeout: 1200
      })
    }).toPass({ timeout: 15000 })
  } finally {
    await guest.close()
  }
  await row.getByRole('button', { name: '删除', exact: true }).click()
  const deleting = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/sys/blog/delete')
  )
  await page.locator('.el-popconfirm:visible .el-button--primary').click()
  expect((await deleting).ok()).toBeTruthy()
  const deleted = await findDeletedArticle(page, title)
  await deleted.getByRole('button', { name: '恢复', exact: true }).click()
  await expect(deleted).toHaveCount(0)
})
