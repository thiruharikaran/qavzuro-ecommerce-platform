import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E configuration. Per the project spec, tests target
 * Microsoft Edge (Chromium "msedge" channel) rather than the default
 * bundled Chromium. Run `npx playwright install msedge` once before
 * the first run if Edge isn't already installed.
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  expect: { timeout: 5_000 },
  fullyParallel: false, // e2e flow tests share seeded data and run best sequentially
  retries: process.env.CI ? 1 : 0,
  reporter: [['html', { open: 'never' }], ['list']],
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'Microsoft Edge',
      use: { ...devices['Desktop Edge'], channel: 'msedge' },
    },
  ],
})
