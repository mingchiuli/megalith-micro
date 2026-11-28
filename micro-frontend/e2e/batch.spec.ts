import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'

for (const [view, resource] of [
  ['users', 'user'],
  ['roles', 'role'],
  ['authorities', 'authority']
]) {
  test(`select, cancel and confirm a two-row deletion in ${view}`, async ({ page }) => {
    await login(page, '/sys/' + view)
    const origin = new URL(page.url()).origin
    const prefix = 'batch' + randomUUID().slice(0, 6)
    const names = [prefix + 'a', prefix + 'b']
    for (const name of names) {
      const data =
        resource === 'user'
          ? {
              username: name,
              nickname: name,
              password: 'FixtureBatch123',
              roles: ['user'],
              status: 0,
              email: name + '@example.org',
              phone: '136' + String(Math.floor(Math.random() * 1e8)).padStart(8, '0'),
              avatar: origin + '/favicon.ico'
            }
          : resource === 'role'
            ? {
                name,
                code: name,
                remark: name,
                status: 0
              }
            : {
                code: name,
                remark: name,
                status: 0,
                type: 1,
                protocol: 'http',
                methodType: 'GET',
                routePattern: '/fixture/' + name,
                serviceHost: 'micro-base',
                servicePort: 8082
              }
      expect(
        (
          await page.request.post(origin + '/api/sys/' + resource + '/save', {
            headers: { Origin: origin },
            data
          })
        ).ok()
      ).toBeTruthy()
    }
    const showRows = async () => {
      await page.reload()
      if (resource !== 'authority') {
        await page.locator('.el-pagination__sizes .el-select').click()
        await page.getByRole('option', { name: /50/ }).click()
      }
    }
    await showRows()
    const batch = page.getByRole('button', { name: '批量删除', exact: true })
    await expect(batch).toBeDisabled()
    for (const name of names) {
      const row = page.getByRole('row').filter({ hasText: name })
      await row.locator('.el-checkbox').click()
    }
    await expect(batch).toBeEnabled()
    await batch.click()
    const confirm = page.locator('.el-popconfirm:visible')
    await confirm.locator('button').first().click()
    for (const name of names)
      await expect(page.getByRole('row').filter({ hasText: name })).toBeVisible()
    const mutation = page.waitForResponse(
      (r) =>
        r.request().method() === 'POST' &&
        new URL(r.url()).pathname === '/api/sys/' + resource + '/delete'
    )
    await batch.click()
    await confirm.locator('button.el-button--primary').click()
    const response = await mutation
    expect(response.ok()).toBeTruthy()
    expect(response.request().postDataJSON()).toHaveLength(2)
    await showRows()
    for (const name of names)
      await expect(page.getByRole('row').filter({ hasText: name })).toHaveCount(0)
  })
}
