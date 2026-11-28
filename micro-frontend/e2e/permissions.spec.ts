import { expect, test } from '@playwright/test'
import { randomUUID } from 'node:crypto'
import { login } from './session'

test('an existing browser session picks up an administrator’s menu grant and revocation', async ({
  page,
  browser
}) => {
  test.setTimeout(60000)
  await login(page, '/sys/roles')
  const name = 'grant' + randomUUID().slice(0, 8)
  const origin = new URL(page.url()).origin
  const write = async (path: string, data: unknown) => {
    const result = await page.request.post(origin + '/api' + path, {
      data,
      headers: { Origin: origin }
    })
    expect(result.status()).toBe(200)
  }
  const read = async <T>(path: string) => {
    const res = await page.request.get(origin + '/api' + path, { headers: { Origin: origin } })
    expect(res.status()).toBe(200)
    return ((await res.json()) as { data: T }).data
  }
  await write('/sys/role/save', { name, code: name, remark: 'browser fixture', status: 0 })
  const roles = await read<{ id: number; code: string }[]>('/sys/role/valid/all')
  const roleId = roles.find((r) => r.code === name)!.id
  interface Menu {
    menuId: number
    children: Menu[]
  }
  const menus = await read<Menu[]>('/sys/role/menu/' + roleId)
  const all = (menu: Menu): number[] => [menu.menuId, ...menu.children.flatMap(all)]
  const root = menus[0].menuId
  await write('/sys/role/menu/' + roleId, [root])
  await write('/sys/user/save', {
    username: name,
    nickname: name,
    password: 'GrantFixture123',
    avatar: origin + '/favicon.ico',
    email: name + '@example.org',
    phone: '137' + String(Math.floor(Math.random() * 1e8)).padStart(8, '0'),
    status: 0,
    roles: [name]
  })
  const users = await read<{ content: { id: number; username: string }[] }>(
    '/sys/user/page/1?size=100'
  )
  const userId = users.content.find((u) => u.username === name)!.id
  const member = await browser.newContext({
    baseURL: origin,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    locale: 'en-US'
  })
  try {
    const session = await member.newPage()
    await session.goto('/login')
    await session.locator('input[placeholder]').first().fill(name)
    await session.locator('input[placeholder]').last().fill('GrantFixture123')
    await session.locator('.dialog-footer button:visible').click()
    await expect(session.locator('.header-dropdown')).toContainText(name)
    await session.goto('/sys/users')
    await expect(session.getByRole('button', { name: '新增', exact: true })).toHaveCount(0)
    await write('/sys/role/menu/' + roleId, menus.flatMap(all))
    await expect(async () => {
      await session.goto('/sys/users')
      await expect(session.getByRole('button', { name: '新增', exact: true })).toBeVisible({
        timeout: 800
      })
    }).toPass({ timeout: 12000 })
    await write('/sys/role/menu/' + roleId, [root])
    await expect(async () => {
      await session.goto('/sys/users')
      await expect(session.getByRole('button', { name: '新增', exact: true })).toHaveCount(0, {
        timeout: 800
      })
    }).toPass({ timeout: 12000 })
  } finally {
    await member.close()
    await write('/sys/user/delete', [userId])
    await write('/sys/role/delete', [roleId])
  }
})
