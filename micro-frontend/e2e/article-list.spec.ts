import { expect, test } from '@playwright/test'
import { readFile } from 'node:fs/promises'
import { randomUUID } from 'node:crypto'
import { login } from './session'
import { findDeletedArticle } from './recycle-bin'

import { prepareArticles } from './articles'

test('readers search and page through indexed articles, and administrators filter, export and batch delete them', async ({
  page,
  browser
}) => {
  test.setTimeout(120000)
  await login(page, '/sys/blogs')
  const origin = new URL(page.url()).origin
  const prefix = 'list' + randomUUID().slice(0, 7)
  const ids = await prepareArticles(page, prefix)
  const search = page.locator('.search-input input')
  await search.fill(prefix)
  const searchButton = page.getByRole('button', { name: '搜索', exact: true })
  const rows = page.locator('.el-table__body-wrapper tr')
  const query = () =>
    page.waitForResponse((r) => new URL(r.url()).pathname.endsWith('/sys/blog/blogs'))
  await expect(async () => {
    const result = query()
    await searchButton.click()
    const body = (await (await result).json()) as { data: { totalElements: number } }
    expect(body.data.totalElements).toBe(ids.length)
  }).toPass({ timeout: 15000 })
  const filters = page.locator('.button-form')
  await filters.locator('.el-select').click()
  await page.getByRole('option', { name: /^(Public|公开)$/i }).click()
  const filtered = query()
  await search.press('Enter')
  expect(new URL((await filtered).url()).searchParams.get('status')).toBe('0')

  const sizes = page.locator('.el-pagination__sizes .el-select')
  await sizes.click()
  await page.getByRole('option', { name: /^5\D/ }).click()
  await expect(rows).toHaveCount(5)
  await page.locator('.el-pagination .btn-next').click()
  await expect(rows).toHaveCount(2)
  await page.locator('.el-pagination .btn-prev').click()
  await expect(rows).toHaveCount(5)
  const download = page.waitForEvent('download')
  await page.getByRole('button', { name: '内容导出', exact: true }).click()
  const file = await download
  expect(file.suggestedFilename()).toMatch(/\.sql$/)
  const sql = await readFile((await file.path())!, 'utf8')
  for (let i = 1; i <= ids.length; i++) expect(sql).toContain(prefix + ' ' + i)

  const guest = await browser.newContext({
    baseURL: origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    locale: 'en-US'
  })
  try {
    const reader = await guest.newPage()
    // Publication reaches the public and administration indexes independently.
    await expect
      .poll(
        async () => {
          const response = await guest.request.get('/api/search/public/blog', {
            params: { keywords: prefix, currentPage: 1, allInfo: true }
          })
          expect(response.ok()).toBeTruthy()
          return ((await response.json()) as { data: { totalElements: number } }).data.totalElements
        },
        { timeout: 30000 }
      )
      .toBe(ids.length)
    await reader.goto('/blogs')
    await reader.locator('.search-button').click()
    await reader.getByRole('dialog').locator('input').fill(prefix)
    await reader
      .getByRole('dialog')
      .getByRole('button', { name: /Confirm|确定/ })
      .press('Enter')
    const cards = reader.locator('.el-card').filter({ hasText: prefix })
    await expect(cards.first()).toBeVisible()
    await reader.locator('.infinite-footer').scrollIntoViewIfNeeded()
    await expect(cards).toHaveCount(ids.length)
    await cards.first().click()
    await expect(reader.locator('.exhibit-title')).toContainText(prefix)
    const toc = reader.locator('.affix .el-tree')
    if (await toc.isVisible()) {
      await toc.getByText('Closing', { exact: true }).click()
      await expect.poll(() => reader.evaluate(() => window.scrollY)).toBeGreaterThan(0)
    }
  } finally {
    await guest.close()
  }

  const first = rows.first()
  await first.getByRole('button', { name: '查看', exact: true }).click()
  await expect(page.locator('.exhibit-title')).toContainText(prefix)
  await page.goBack()
  await search.fill(prefix)
  await searchButton.click()
  await expect(rows.first()).toContainText(prefix)
  await rows.first().getByRole('button', { name: '编辑', exact: true }).click()
  await expect(page.locator('.title input')).toHaveValue(new RegExp(prefix))
  await page.goBack()
  await search.fill(prefix)
  await searchButton.click()
  await expect(rows.first()).toContainText(prefix)
  for (const row of [rows.nth(0), rows.nth(1)]) {
    await expect(row).toContainText(prefix)
    await row.locator('.el-checkbox').click()
  }
  const deletion = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/sys/blog/delete')
  )
  await page.getByRole('button', { name: '批量删除', exact: true }).click()
  await page.locator('.el-popconfirm:visible .el-button--primary').click()
  const removed = await deletion
  expect(removed.ok()).toBeTruthy()
  const removedIds = removed.request().postDataJSON() as number[]
  expect(removedIds).toHaveLength(2)
  expect(removedIds.every((id) => ids.includes(id))).toBe(true)
  for (const id of removedIds) await findDeletedArticle(page, prefix + ' ' + (ids.indexOf(id) + 1))
})
