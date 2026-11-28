import { expect, test } from '@playwright/test'
import { execFile } from 'node:child_process'
import { promisify } from 'node:util'
import { login } from '../e2e/session'

const run = promisify(execFile)
function setting(name: string): string {
  const value = process.env[name]
  if (!value) throw new Error('Missing live fixture setting ' + name)
  return value
}

for (const channel of ['EMAIL', 'SMS']) {
  test(channel + ' sends a code to the designated test account and logs in', async ({ page }) => {
    test.setTimeout(180000)
    const recipient = setting('E2E_' + channel + '_RECIPIENT')
    const readCode = setting('E2E_RECEIVE_CODE_PROGRAM')
    await page.goto('/login')
    await page.locator('label:has(input[value="' + channel + '"])').click()
    await page.locator('input[placeholder]').first().fill(recipient)
    const sent = page.waitForResponse((r) =>
      new URL(r.url()).pathname.endsWith('/code/' + channel.toLowerCase())
    )
    const since = new Date().toISOString()
    await page.locator('.dialog-footer button:visible').last().click()
    expect((await sent).status()).toBe(200)
    const { stdout } = await run(readCode, [channel, recipient, since], { timeout: 115000 })
    await page.locator('input[placeholder]').last().fill(stdout.trim())
    const accepted = page.waitForResponse((r) => new URL(r.url()).pathname.endsWith('/api/login'))
    await page.locator('.dialog-footer button:visible').first().click()
    expect((await accepted).status()).toBe(200)
    await expect(page.locator('.header-dropdown')).toBeVisible()
  })
}

test('cover upload and deletion use the fixture OSS bucket', async ({ page }) => {
  const assetOrigin = setting('E2E_OSS_ORIGIN')
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const uploaded = page.waitForResponse(
    (r) =>
      r.request().method() === 'POST' && new URL(r.url()).pathname.endsWith('/sys/blog/oss/upload')
  )
  await page
    .locator('.el-upload input[type=file]')
    .first()
    .setInputFiles({
      name: 'fixture.png',
      mimeType: 'image/png',
      buffer: Buffer.from(
        'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO7+JvsAAAAASUVORK5CYII=',
        'base64'
      )
    })
  const response = await uploaded
  expect(response.ok()).toBeTruthy()
  const { data } = (await response.json()) as { data: string }
  expect(new URL(data).origin).toBe(assetOrigin)
  expect((await page.request.get(data)).ok()).toBeTruthy()
  const deleted = page.waitForResponse(
    (r) =>
      r.request().method() === 'DELETE' &&
      new URL(r.url()).pathname.endsWith('/sys/blog/oss/delete')
  )
  await page.locator('.el-upload-list__item').first().hover()
  await page.locator('.el-upload-list__item-delete').first().click()
  expect((await deleted).ok()).toBeTruthy()
  await expect.poll(async () => (await page.request.get(data)).status()).toBe(404)
})

test('Ollama generates article metadata and a preview with the installed models', async ({
  page
}) => {
  test.setTimeout(900000)
  const model = setting('E2E_OLLAMA_MODEL')
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const editor = page.locator('#md-editor .cm-content')
  await expect(editor).toHaveAttribute('contenteditable', 'true')
  await editor.click()
  await page.keyboard.insertText(
    'A garden study. Small trays of basil grew more leaves when watered in the morning.'
  )
  await page.locator('.ai-actions .el-select').click()
  await page.getByRole('option', { name: model, exact: true }).click()
  const image = page.waitForResponse(
    (response) => {
      if (!new URL(response.url()).pathname.endsWith('/api/generate')) return false
      const body = response.request().postDataJSON() as { model?: string }
      return body.model === (process.env.E2E_OLLAMA_IMAGE_MODEL || 'x/flux2-klein:9b-bf16')
    },
    { timeout: 600000 }
  )
  await page.locator('.ai-actions button').click()
  await expect(page.locator('.title input')).not.toHaveValue('', { timeout: 300000 })
  await expect(page.locator('.desc :is(input,textarea)')).not.toHaveValue('')
  expect((await image).status(), 'image model generation request').toBe(200)
  await expect(page.locator('.image-preview-container img')).toBeVisible({ timeout: 500000 })
})

test('article discussion tabs load the designated Giscus repository and Disqus site', async ({
  page
}) => {
  const article = setting('E2E_COMMENT_ARTICLE_PATH')
  const repo = setting('E2E_GISCUS_REPO')
  const shortname = setting('E2E_DISQUS_SHORTNAME')
  await page.goto(article, { waitUntil: 'domcontentloaded' })
  const comments = page.locator('#comment-div')
  await comments.getByRole('tab', { name: 'Giscus' }).click()
  await expect(page.locator('#giscus_thread script')).toHaveAttribute('data-repo', repo)
  const giscus = page.frameLocator('#giscus_thread iframe')
  await expect(giscus.locator('body')).toBeVisible()
  await comments.getByRole('tab', { name: 'Disqus' }).click()
  await expect(page.locator('#disqus_thread script[src]').first()).toHaveAttribute(
    'src',
    'https://' + shortname + '.disqus.com/embed.js'
  )
  await expect(page.frameLocator('#disqus_thread iframe').first().locator('body')).toBeVisible()
})
