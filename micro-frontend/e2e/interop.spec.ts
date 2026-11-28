import { expect, test } from '@playwright/test'
import { login } from './session'

test('the peer release serves an existing session and rotates only the access cookie', async ({
  page,
  context,
  browser
}) => {
  const peerUrl = process.env.E2E_PEER_BASE_URL
  if (!peerUrl) throw new Error('Set E2E_PEER_BASE_URL to the other isolated release')
  await login(page)
  const existing = (await context.cookies()).filter((c) =>
    ['megalith_access_token', 'megalith_refresh_token'].includes(c.name)
  )
  const afterCutover = await browser.newContext({
    baseURL: peerUrl,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1'
  })
  try {
    await afterCutover.addCookies(
      existing.map((c) => ({ ...c, domain: new URL(peerUrl).hostname }))
    )
    const peer = await afterCutover.newPage()
    await peer.goto('/backend')
    await expect(peer.locator('.header-dropdown')).toContainText('E2E Admin')
    await afterCutover.clearCookies({ name: 'megalith_access_token' })
    await peer.reload()
    await expect(peer.locator('.header-dropdown')).toContainText('E2E Admin')
    const renewed = await afterCutover.cookies()
    const access = renewed.find((c) => c.name === 'megalith_access_token')
    expect(access?.value === existing.find((c) => c.name === 'megalith_access_token')?.value).toBe(
      false
    )
    expect(access?.httpOnly).toBe(true)
    expect(
      renewed.find((c) => c.name === 'megalith_refresh_token')?.value ===
        existing.find((c) => c.name === 'megalith_refresh_token')?.value
    ).toBe(true)
  } finally {
    await afterCutover.close()
  }
})
