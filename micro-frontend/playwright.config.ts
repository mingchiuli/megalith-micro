import { defineConfig, devices } from '@playwright/test'

const baseURL = process.env.E2E_BASE_URL
if (!baseURL) throw new Error('Set E2E_BASE_URL to the isolated application entry point')

export default defineConfig({
  testDir: './e2e',
  retries: 0,
  workers: 1,
  reporter: [
    ['list'],
    ['html', { open: 'never', outputFolder: 'build/playwright-report' }],
    ['json', { outputFile: process.env.E2E_REPORT || 'build/playwright-results.json' }]
  ],
  outputDir: 'build/playwright-results',
  use: {
    baseURL,
    ignoreHTTPSErrors: process.env.E2E_SELF_SIGNED === '1',
    locale: 'en-US',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit', use: { ...devices['Desktop Safari'] } },
    { name: 'mobile', use: { ...devices['iPhone 13'] } }
  ]
})
