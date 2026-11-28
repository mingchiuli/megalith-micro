import { expect, type Page } from '@playwright/test'
import { findTableRow } from './table'

export async function findDeletedArticle(page: Page, title: string) {
  await page.goto('/sys/delete')
  // The recycle bin becomes visible after the delete event reaches its consumer.
  await expect(async () => {
    await page.reload()
    await findTableRow(page, title)
  }).toPass({ timeout: 20000 })
  return page.getByRole('row').filter({ has: page.getByRole('cell', { name: title, exact: true }) })
}
