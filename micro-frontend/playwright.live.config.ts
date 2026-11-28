import { defineConfig } from '@playwright/test'
import config from './playwright.config'

export default defineConfig({
  ...config,
  testDir: './e2e-live',
  outputDir: 'build/playwright-live-results',
  workers: 1
})
