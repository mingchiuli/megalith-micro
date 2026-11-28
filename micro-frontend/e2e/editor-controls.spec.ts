import { expect, test, type Page } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import type { BlogVersionSaveResponse } from '../src/type/entity'
import { login } from './session'

function fixtureWriter(page: Page) {
  const origin = new URL(page.url()).origin
  return async function post<T>(path: string, data: unknown): Promise<T> {
    const result = await page.request.post(origin + '/api' + path, {
      headers: { Origin: origin },
      data
    })
    expect(result.ok()).toBeTruthy()
    return ((await result.json()) as { data: T }).data
  }
}

test('a retained article draft from the legacy browser database opens as a read-only recovery copy', async ({
  page
}) => {
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  await expect(page.getByRole('button', { name: /^(Publish|发布)$/ })).toBeEnabled()
  const id = new URL(page.url()).searchParams.get('id')!
  await page.evaluate(async (blogId) => {
    await new Promise<void>((resolve, reject) => {
      const request = indexedDB.open('megalith-editor-drafts-v1')
      request.onerror = () => reject(request.error)
      request.onsuccess = () => {
        const db = request.result
        const tx = db.transaction('metadata', 'readwrite')
        tx.onerror = () => reject(tx.error)
        tx.oncomplete = () => {
          db.close()
          resolve()
        }
        tx.objectStore('metadata').put({
          key: 'megalith:editor:metadata:v1:1:' + blogId,
          title: 'Retained draft',
          description: 'Previous editor metadata',
          content: 'A local paragraph from the old browser database.',
          status: 0,
          link: '',
          sensitiveContentList: [],
          updatedAt: Date.now()
        })
      }
    })
  }, id)
  await page.reload()
  const copy = page.locator('.el-collapse').filter({ hasText: 'Retained draft' })
  await copy.locator('.el-collapse-item__header').click()
  const fields = copy.locator('input, textarea')
  for (const [i, expected] of [
    'Retained draft',
    'Previous editor metadata',
    'A local paragraph from the old browser database.'
  ].entries()) {
    await expect(fields.nth(i)).toHaveValue(expected)
    await expect(fields.nth(i)).toHaveJSProperty('readOnly', true)
  }
  await expect(page.locator('.title input')).not.toHaveValue('Retained draft')
})

test('retrying a failed collaboration ticket unlocks editing, and the PDF preview renders the draft', async ({
  page,
  browserName
}) => {
  await login(page)
  await page.route('**/api/sys/blog/edit/ticket*', (route) => route.abort('connectionfailed'), {
    times: 1
  })
  await page.goto('/sys/edit')
  const retry = page.locator('.collaboration-status button')
  await expect(retry).toBeVisible()
  const ticket = page.waitForResponse((r) =>
    new URL(r.url()).pathname.endsWith('/sys/blog/edit/ticket')
  )
  await retry.click()
  expect((await ticket).ok()).toBeTruthy()
  const editor = page.locator('#md-editor .cm-content')
  await expect(editor).toHaveAttribute('contenteditable', 'true')
  await editor.click()
  await page.keyboard.insertText('# Print preview\n\nRetained paragraph.')
  await page
    .locator('#md-editor [title]')
    .filter({ has: page.locator('svg.lucide-printer') })
    .first()
    .click()
  const preview = page.locator('#export-pdf-preview')
  await expect(preview.getByRole('heading', { name: 'Print preview' })).toBeVisible()
  await expect(preview).toContainText('Retained paragraph.')
  await expect(page.getByRole('button', { name: /^(Export|导出)$/ })).toBeVisible()
  if (browserName === 'chromium') {
    const bytes = await page.pdf({ format: 'A4' })
    expect(bytes.subarray(0, 5).toString()).toBe('%PDF-')
  }
})

test('the administrator applies and clears a creation-date range', async ({ page }) => {
  await login(page, '/sys/blogs')
  const range = page.locator('.el-date-editor--datetimerange')
  await range.locator('input').first().click()
  const panel = page.locator('.el-date-range-picker:visible')
  const days = panel.locator('.is-left td.available')
  await days.nth(0).click()
  await days.nth(2).click()
  await panel.getByRole('button', { name: /^(OK|Confirm|确定)$/i }).click()
  const response = () =>
    page.waitForResponse((r) => new URL(r.url()).pathname.endsWith('/sys/blog/blogs'))
  const applied = response()
  await page.getByRole('button', { name: '搜索', exact: true }).click()
  const query = new URL((await applied).url()).searchParams
  expect(query.get('createStart')).toMatch(/^\d{4}-\d{2}-01T/)
  expect(query.get('createEnd')).toMatch(/^\d{4}-\d{2}-03T/)
  const cleared = response()
  await range.hover()
  await range.locator('.el-range__close-icon').click()
  expect(new URL((await cleared).url()).searchParams.get('createStart')).toBeFalsy()
  await expect(range.locator('input').first()).toHaveValue('')
})

test('the version drawer pages through eleven revisions and searches for a single earlier edition', async ({
  page
}) => {
  test.setTimeout(75000)
  await login(page, '/sys/blogs')
  const post = fixtureWriter(page)
  const draft = await post<{ id: number; collaborationEpoch: string }>('/sys/blog/draft/new', {})
  const title = 'revisions' + draft.id
  const content = {
    ...draft,
    title,
    description: 'Archive fixture',
    status: 0,
    link: '',
    sensitiveContentList: []
  }
  let draftRevision = (
    await post<{ draftRevision: number }>(`/sys/blog/${draft.id}/draft`, {
      ...content,
      content: 'Original'
    })
  ).draftRevision
  let revisionId = 0
  for (let n = 1; n <= 11; n++) {
    const saved = await post<BlogVersionSaveResponse>(`/sys/blog/${draft.id}/versions`, {
      ...content,
      draftRevision,
      content: n === 2 ? 'vintageuniqueword' : 'Current article ' + n
    })
    draftRevision = saved.draftRevision
    revisionId = saved.version.id
  }
  await post(`/sys/blog/${draft.id}/publish`, { revisionId })
  const search = page.locator('.search-input input')
  await search.fill(title)
  const row = page.getByRole('row').filter({ hasText: title }).first()
  await expect(async () => {
    await search.press('Enter')
    await expect(row).toBeVisible({ timeout: 1500 })
  }).toPass({ timeout: 20000 })
  await row.getByRole('button', { name: /^(Version history|版本历史)$/ }).click()
  const history = page.locator('.el-drawer:visible')
  const rows = history.locator('.el-table__body-wrapper tbody tr')
  await expect(async () => {
    await history.getByRole('button', { name: /^(Search|搜索)$/ }).click()
    await expect(rows).toHaveCount(10, { timeout: 1200 })
    await expect(history.locator('.btn-next')).toBeEnabled()
  }).toPass({ timeout: 20000 })
  await history.locator('.btn-next').click()
  await expect(rows).toHaveCount(1)
  await expect(rows.first().locator('td').first()).toHaveText('1')
  const keyword = history.locator('.version-search input')
  await keyword.fill('vintageuniqueword')
  await keyword.press('Enter')
  await expect(rows.first().locator('td').first()).toHaveText('2')
  await expect(rows).toHaveCount(1)
})

test('an editor selects a description span and chooses between repeated words in the body', async ({
  page
}) => {
  await login(page)
  await page.goto('/sys/edit')
  await expect(page).toHaveURL(/\/sys\/edit\?id=\d+$/)
  const editor = page.locator('#md-editor .cm-content')
  await expect(editor).toHaveAttribute('contenteditable', 'true')
  await page.locator('.status label:has(input[value="2"])').click()
  const description = page.locator('.desc textarea')
  await description.fill('a test')
  await description.evaluate((input: HTMLTextAreaElement) => input.setSelectionRange(0, 0))
  await description.press('Shift+ArrowRight')
  const tags = page.locator('.el-form-item .el-tag')
  await expect(tags).toHaveText(['0'])
  await expect(page.getByRole('button', { name: /^(Publish|发布)$/ })).toBeEnabled()
  await editor.click()
  await page.keyboard.insertText('echo second echo')
  const line = editor.locator('.cm-line').first()
  await expect(line).toHaveText('echo second echo')
  await line.scrollIntoViewIfNeeded()
  const word = await line.evaluate((element) => {
    const range = document.createRange()
    const word = document.createTreeWalker(element, NodeFilter.SHOW_TEXT).nextNode()!
    range.setStart(word, 0)
    range.setEnd(word, 4)
    const rect = range.getBoundingClientRect()
    return { left: rect.left, right: rect.right, y: rect.top + rect.height / 2 }
  })
  await page.mouse.move(word.left + 1, word.y)
  await page.mouse.down()
  await page.mouse.move(word.right - 1, word.y, { steps: 12 })
  await page.mouse.up()
  const dialog = page.locator('.el-dialog:visible').filter({ has: page.locator('mark') })
  await expect(dialog.locator('mark')).toHaveText(['echo', 'echo'])
  await dialog.locator('.el-table__body-wrapper tr').nth(1).click()
  await expect(dialog).toHaveCount(0)
  await expect(tags).toHaveText(['0', '12'])
})

test('search cards handle loaded and missing covers and autocomplete opens the matching article', async ({
  page,
  browser
}) => {
  test.setTimeout(80000)
  await login(page)
  const origin = new URL(page.url()).origin
  const post = fixtureWriter(page)
  const ids: number[] = []
  const prefix = 'image' + randomUUID().slice(0, 8)
  for (const [i, path] of ['/favicon.ico', '/missing-regression-cover.png'].entries()) {
    const draft = await post<{ id: number; collaborationEpoch: string }>('/sys/blog/draft/new', {})
    const row = {
      ...draft,
      title: prefix + ' ' + i,
      content: 'Cover fixture',
      description: 'Image loading',
      link: origin + path,
      status: 0,
      sensitiveContentList: []
    }
    const saved = await post<{ draftRevision: number }>(`/sys/blog/${draft.id}/draft`, row)
    const version = await post<BlogVersionSaveResponse>(`/sys/blog/${draft.id}/versions`, {
      ...row,
      draftRevision: saved.draftRevision
    })
    await post(`/sys/blog/${draft.id}/publish`, { revisionId: version.version.id })
    ids.push(draft.id)
  }
  const guest = await browser.newContext({
    baseURL: origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    locale: 'en-US'
  })
  try {
    await expect
      .poll(
        async () => {
          const res = await guest.request.get('/api/search/public/blog', {
            params: { keywords: prefix, allInfo: true, currentPage: 1 }
          })
          const { data } = (await res.json()) as { data: { content: Array<{ id: number }> } }
          return ids.every((id) => data.content.some((item) => item.id === id))
        },
        { timeout: 30000 }
      )
      .toBe(true)
    const reader = await guest.newPage()
    await reader.goto('/blogs?q=' + prefix)
    const cards = reader.locator('.el-card')
    const valid = cards.filter({ hasText: prefix + ' 0' })
    await expect(valid.locator('.el-image img')).toBeVisible({ timeout: 15000 })
    await expect
      .poll(() =>
        valid.locator('.el-image img').evaluate((img: HTMLImageElement) => img.naturalWidth)
      )
      .toBeGreaterThan(0)
    await expect(cards.filter({ hasText: prefix + ' 1' }).locator('.el-image__error')).toBeVisible()
    await reader.locator('.search-button').click()
    const field = reader.getByRole('dialog').locator('input')
    await field.fill(prefix + ' 0')
    const suggestion = reader
      .locator('.select-list [role=option]')
      .filter({ hasText: prefix + ' 0' })
      .first()
    await suggestion.click()
    await expect(reader).toHaveURL(new RegExp('/blog/' + ids[0] + '$'))
    await expect(reader.locator('.exhibit-title')).toHaveText(prefix + ' 0')
  } finally {
    await guest.close()
  }
})
