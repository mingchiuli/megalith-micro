import { expect, test, type Page, type Locator } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'
import { findTableRow } from './table'

const unique = () => 'e2e' + randomUUID().slice(0, 8)
const button = (scope: Page | Locator, name: string) =>
  scope.getByRole('button', { name, exact: true })

async function save(page: Page, action = '保存') {
  const dialog = page.getByRole('dialog')
  await button(dialog, action).click()
  await expect(dialog).toBeHidden()
}

async function remove(page: Page, row: Locator) {
  await button(row, '删除').click()
  await page.locator('.el-popconfirm:visible .el-button--primary').click()
  await expect(row).toHaveCount(0)
  await page.reload()
  await expect(row).toHaveCount(0)
}

test('role create, rename, menu assignment, data permissions, deletion', async ({ page }) => {
  await login(page, '/sys/roles')
  const name = unique()
  await button(page, '新增').click()
  const dialog = page.getByRole('dialog')
  const texts = dialog.getByRole('textbox')
  await texts.nth(0).fill(name)
  await texts.nth(1).fill(name)
  await texts.nth(2).fill('browser fixture')
  await save(page)
  const row = await findTableRow(page, name)
  await button(row, '编辑').click()
  await texts.nth(1).fill(name + 'v2')
  await save(page)
  await page.reload()
  await findTableRow(page, name)
  await expect(row).toContainText(name + 'v2')
  await button(row, '菜单权限').click()
  const tree = dialog.getByRole('tree')
  await tree.locator('.el-checkbox').first().click()
  await save(page, '保存权限')
  await button(row, '数据权限').click()
  await dialog.locator('.el-checkbox').first().click()
  await save(page, '保存数据权限')
  await page.reload()
  await findTableRow(page, name)
  await button(row, '数据权限').click()
  await expect(dialog.getByRole('checkbox').first()).toBeChecked()
  await dialog.locator('.el-dialog__headerbtn').click()
  await remove(page, row)
})

test('user creation, profile edit with an empty password, and deletion', async ({
  page,
  browser
}) => {
  await login(page, '/sys/users')
  const name = unique()
  const secret = 'BrowserFixture123'
  await button(page, '新增').click()
  const dialog = page.getByRole('dialog')
  for (const [field, value] of Object.entries({
    username: name,
    nickname: name,
    password: secret,
    avatar: new URL('/favicon.ico', page.url()).href,
    email: name + '@example.org',
    phone: '138' + String(Math.floor(Math.random() * 1e8)).padStart(8, '0')
  })) {
    await dialog.locator('.' + field + ' input').fill(value)
  }
  await dialog.locator('.el-select').click()
  await page.getByRole('option', { name: 'E2E Admin', exact: true }).click()
  await dialog.locator('.el-select input').press('Escape')
  await save(page)
  const row = await findTableRow(page, name)
  await button(row, '编辑').click()
  await dialog.locator('.nickname input').fill(name + 'edited')
  await dialog.locator('.password input').fill('')
  await save(page)
  await page.reload()
  await findTableRow(page, name)
  await expect(row).toContainText(name + 'edited')
  const context = await browser.newContext({
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1'
  })
  try {
    const member = await context.newPage()
    await member.goto(new URL('/login', page.url()).href)
    await member.locator('input[placeholder]').first().fill(name)
    await member.locator('input[placeholder]').last().fill(secret)
    await member.locator('.dialog-footer button:visible').click()
    await expect(member.locator('.header-dropdown')).toContainText(name + 'edited')
  } finally {
    await context.close()
  }
  await remove(page, row)
})

test('create, edit, and delete one authority route', async ({ page }) => {
  await login(page, '/sys/authorities')
  const name = unique()
  await button(page, '新增').click()
  const d = page.getByRole('dialog')
  await d.getByRole('textbox').nth(0).fill(name)
  await d
    .getByRole('textbox')
    .nth(1)
    .fill(name + ' note')
  await d
    .getByRole('textbox')
    .nth(2)
    .fill('/fixture/' + name)
  for (const [i, option] of ['http', 'GET', 'micro-auth'].entries()) {
    await d.locator('.el-select').nth(i).click()
    await page.getByRole('option', { name: option, exact: true }).click()
  }
  await save(page)
  const row = page.getByRole('row').filter({ hasText: name })
  await button(row, '编辑').click()
  await d
    .getByRole('textbox')
    .nth(1)
    .fill(name + ' changed')
  await save(page)
  await page.reload()
  await expect(row).toContainText(name + ' changed')
  await remove(page, row)
})

test('create, edit, assign an authority to, and delete a root menu', async ({ page }) => {
  await login(page, '/sys/menus')
  const name = unique()
  await button(page, '新增').click()
  const d = page.getByRole('dialog')
  const inputs = d.locator('.el-input__inner').filter({ visible: true })
  await d.getByLabel(/^(Title|标题)$/).fill(name)
  await d.getByLabel(/^(Component name|组件名称|组件名)$/i).fill(name)
  await d.getByRole('spinbutton').fill('2')
  await save(page)
  const row = page.getByRole('row').filter({ hasText: name })
  await button(row, '编辑').click()
  await inputs.nth(1).fill(name + 'Edit')
  await save(page)
  await page.reload()
  await expect(row).toBeVisible()
  await button(row, '接口权限').click()
  await d.locator('.el-checkbox').first().click()
  await save(page, '保存权限')
  await remove(page, row)
})
