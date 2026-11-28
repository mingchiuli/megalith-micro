import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'

test('an author issues a reading code and a guest can consume it exactly once', async ({
  page,
  browser
}) => {
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const id = new URL(page.url()).searchParams.get('id')!
  const title = 'locked-' + randomUUID().slice(0, 8)
  await page.locator('.title input').fill(title)
  await page.locator('.desc textarea').fill('Limited access article')
  const editor = page.locator('#md-editor .cm-content')
  await expect(editor).toHaveAttribute('contenteditable', 'true')
  await expect(page.getByRole('button', { name: /^(Publish|发布)$/ })).toBeEnabled()
  await editor.click()
  await editor.pressSequentially('Reading code fixture.')
  await expect(editor).toHaveText('Reading code fixture.')
  await page.locator('.status label:has(input[value="1"])').click()
  const published = page.waitForResponse((r) => new URL(r.url()).pathname.endsWith('/publish'))
  await page.getByRole('button', { name: /^(Publish|发布)$/ }).click()
  expect((await published).status()).toBe(200)
  await expect(page).toHaveURL(/\/sys\/blogs$/)
  const row = page.getByRole('row').filter({ hasText: title })
  const issue = row.getByRole('button', { name: '密码', exact: true })
  await expect(async () => {
    await page.reload()
    await expect(issue).toBeVisible({ timeout: 1000 })
  }).toPass({ timeout: 15000 })
  const issued = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/sys/blog/lock/' + id)
  )
  await issue.click()
  const { data: readToken } = (await (await issued).json()) as { data: string }
  const origin = new URL(page.url()).origin
  const guest = await browser.newContext({
    baseURL: origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1'
  })
  try {
    const reader = await guest.newPage()
    await reader.goto('/login')
    const read = (code: string) =>
      reader.evaluate(
        async ({ id, code }) => {
          const response = await fetch('/api/public/blog/secret/' + id, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ readToken: code })
          })
          const body = (await response.json()) as { data?: { title?: string } }
          return { status: response.status, title: body.data?.title }
        },
        { id, code }
      )
    expect((await read('incorrect')).status).toBe(401)
    expect(await read(readToken)).toEqual({ status: 200, title })
    expect((await read(readToken)).status).toBe(401)
  } finally {
    await guest.close()
  }
})
