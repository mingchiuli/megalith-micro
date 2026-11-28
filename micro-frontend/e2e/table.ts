import { expect, type Page } from '@playwright/test'

export async function findTableRow(page: Page, cell: string) {
  const rows = page.locator('.el-table__body-wrapper tbody tr')
  const match = rows.filter({ has: page.getByRole('cell', { name: cell, exact: true }) })
  const next = page.locator('.el-pagination .btn-next')
  await expect(rows.first()).toBeVisible()
  while (!(await match.count()) && (await next.isEnabled())) {
    const previous = await rows.first().innerText()
    await next.click()
    await expect(rows.first()).not.toHaveText(previous)
  }
  await expect(match).toBeVisible({ timeout: 1500 })
  return match
}
