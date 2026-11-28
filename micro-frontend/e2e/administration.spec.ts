import { expect, test } from '@playwright/test'
import { readFile } from 'node:fs/promises'
import { login } from './session'

test('password login creates HttpOnly cookies, restores SSR identity, then logs out', async ({
  page,
  context,
  browser
}) => {
  await login(page)
  const cookies = await context.cookies()
  for (const name of ['megalith_access_token', 'megalith_refresh_token']) {
    const cookie = cookies.find((c) => c.name === name)
    expect(cookie).toMatchObject({ httpOnly: true, secure: true, sameSite: 'Strict', path: '/' })
    expect(await page.evaluate(() => document.cookie)).not.toContain(name)
  }
  await page.reload()
  await expect(page.locator('.header-dropdown')).toContainText('E2E Admin')
  const anonymous = await browser.newContext({
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1'
  })
  try {
    const guest = await anonymous.newPage()
    await guest.goto(new URL('/sys/users', page.url()).href)
    await expect(guest).toHaveURL(/\/login\?redirect=/)
  } finally {
    await anonymous.close()
  }
  await page.locator('.header-dropdown .el-dropdown-link').press('Enter')
  await page.getByText(/^(Log out|退出登录)$/).click()
  await expect
    .poll(
      async () =>
        (await context.cookies()).filter(
          (c) => c.name.startsWith('megalith_') && c.name.endsWith('_token')
        ).length
    )
    .toBe(0)
  await page.goto('/sys/users')
  await expect(page).toHaveURL(/\/login\?redirect=/)
})

for (const [route, table] of [
  ['users', 'm_user'],
  ['roles', 'm_role'],
  ['menus', 'm_menu'],
  ['authorities', 'm_authority']
]) {
  test(`open, validate, cancel, and export ${route}`, async ({ page }) => {
    await login(page, '/sys/' + route)
    await expect(page.locator('.el-table__body-wrapper tr').first()).toBeVisible()
    await page.getByRole('button', { name: '新增', exact: true }).click()
    const dialog = page.getByRole('dialog')
    await expect(dialog).toBeVisible()
    await dialog.getByRole('button', { name: '保存', exact: true }).click()
    await expect(dialog.locator('.el-form-item__error').first()).toBeVisible()
    for (const group of await dialog.locator('.el-radio-group').all()) {
      for (const choice of await group.locator('label').all()) {
        await choice.click()
        await expect(choice.locator('input')).toBeChecked()
      }
    }
    if (route === 'menus') {
      await dialog.locator('.el-select').click()
      await page.getByRole('option', { name: '后台管理系统', exact: true }).click()
      for (const label of [/^(Icon|图标)$/, /^(URL|链接|地址)$/i, /^(Component URI|组件路径)$/i]) {
        const input = dialog.getByLabel(label)
        await input.fill('menu-field')
        await expect(input).toHaveValue('menu-field')
      }
    }
    await dialog.locator('.el-dialog__headerbtn').click()
    await expect(dialog).toBeHidden()
    const download = page.waitForEvent('download')
    await page.getByRole('button', { name: '内容导出', exact: true }).click()
    const file = await download
    const path = await file.path()
    expect(path).toBeTruthy()
    expect(await readFile(path!, 'utf8')).toContain('INSERT INTO ' + table)
  })
}
