import { expect, test } from '@playwright/test'
import { login } from './session'

test('sidebar collapse, dynamic menus, tab selection, tab close and return to the public site', async ({
  page
}) => {
  await login(page)
  const sidebar = page.locator('.el-aside')
  const collapse = sidebar.locator('.collapse-button')
  const expanded = (await collapse.getAttribute('aria-expanded')) === 'true'
  await collapse.click()
  await expect(collapse).toHaveAttribute('aria-expanded', String(!expanded))
  if (expanded) await collapse.click()
  await expect(collapse).toHaveAttribute('aria-expanded', 'true')

  const origin = new URL(page.url()).origin
  const response = await page.request.get(origin + '/api/auth/menu/nav', {
    headers: { Origin: origin }
  })
  expect(response.ok()).toBeTruthy()
  interface Item {
    url: string
    title: string
    children: Item[]
  }
  const tree = ((await response.json()) as { data: Item }).data
  function path(items: Item[], route: string): Item[] {
    for (const item of items) {
      if (item.url === route) return [item]
      const child = path(item.children ?? [], route)
      if (child.length) return [item, ...child]
    }
    return []
  }
  for (const target of ['/sys/users', '/sys/roles']) {
    const chain = path(tree.children, target)
    expect(chain.length).toBeGreaterThan(0)
    for (const parent of chain.slice(0, -1)) {
      const submenu = sidebar.locator('.el-sub-menu__title').filter({ hasText: parent.title })
      if (!(await submenu.locator('..').evaluate((el) => el.classList.contains('is-opened'))))
        await submenu.click()
    }
    await sidebar.getByRole('menuitem', { name: chain.at(-1)!.title, exact: true }).click()
    await expect(page).toHaveURL(new RegExp(target + '$'))
  }
  if ((page.viewportSize()?.width ?? 1200) <= 900) {
    await collapse.click()
    await expect(collapse).toHaveAttribute('aria-expanded', 'false')
  }
  const userTab = page.getByRole('tab', { name: path(tree.children, '/sys/users').at(-1)!.title })
  await userTab.click()
  await expect(page).toHaveURL(/\/sys\/users$/)
  await userTab.locator('.is-icon-close').click()
  await expect(page).toHaveURL(/\/sys\/roles$/)
  await page.getByRole('tab', { selected: true }).locator('.is-icon-close').click()
  await expect(page).toHaveURL(/\/backend$/)
  await page.locator('.header-dropdown .el-dropdown-link').press('Enter')
  await page.getByRole('menuitem', { name: /^(Back to home|回到首页)$/ }).click()
  await expect(page).toHaveURL(/\/blogs$/)
  await page
    .locator('.door')
    .filter({ hasText: /admin|后台/i })
    .click()
  await expect(page).toHaveURL(/\/backend$/)
})

test('404 calendar changes months, selects a date and links back to the homepage', async ({
  page
}) => {
  await page.goto('/missing-calendar-page')
  const calendar = page.locator('.el-calendar')
  const heading = calendar.locator('.el-calendar__title')
  const start = await heading.innerText()
  await calendar.getByRole('button', { name: /next month|下个月/i }).click()
  await expect(heading).not.toHaveText(start)
  const date = calendar.locator('td.current').filter({ hasText: /^15$/ })
  await date.click()
  await expect(date).toHaveClass(/is-selected/)
  await calendar.getByRole('button', { name: /prev(?:ious)? month|上个月/i }).click()
  await expect(heading).toHaveText(start)
  await calendar.getByRole('button', { name: /today|今天/i }).click()
  await expect(calendar.locator('.is-today')).toHaveClass(/is-selected/)
  await page.locator('footer a.copyright').click()
  await expect(page.getByAltText('Megalith')).toBeVisible()
})
