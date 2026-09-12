import { test, expect } from '@playwright/test'

/**
 * Verifies authorization boundaries: a plain customer must not see or reach
 * admin-only areas, while a seeded manager account can. The seeded accounts
 * (customer@qavzuro.dev / manager@qavzuro.dev) must exist - i.e. the backend
 * must have run with app.seed.enabled=true at least once.
 */
test.describe('Role-based access control', () => {
  test('a customer cannot see or navigate to the admin section', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('Email').fill('customer@qavzuro.dev')
    await page.getByLabel('Password').fill('Customer123!')
    await page.getByRole('button', { name: /sign in/i }).click()
    await expect(page).toHaveURL('/')

    await expect(page.getByRole('link', { name: 'Admin' })).toHaveCount(0)

    // Even a direct navigation attempt must be redirected away.
    await page.goto('/admin')
    await expect(page).not.toHaveURL('/admin')
  })

  test('a manager can reach the admin dashboard and see permitted sections only', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('Email').fill('manager@qavzuro.dev')
    await page.getByLabel('Password').fill('Manager123!')
    await page.getByRole('button', { name: /sign in/i }).click()

    await page.getByRole('link', { name: 'Admin' }).click()
    await expect(page).toHaveURL('/admin')
    await expect(page.getByText(/total revenue|revenue/i)).toBeVisible()

    // Managers have ORDER_UPDATE/RETURN_APPROVE etc. but not ROLE_MANAGE -
    // the Roles & Permissions nav item must not be rendered for them.
    await expect(page.getByRole('link', { name: /roles & permissions/i })).toHaveCount(0)
  })

  test('a worker can reach the workforce dashboard but not admin', async ({ page }) => {
    await page.goto('/login')
    await page.getByLabel('Email').fill('worker@qavzuro.dev')
    await page.getByLabel('Password').fill('Worker123!')
    await page.getByRole('button', { name: /sign in/i }).click()

    await page.getByRole('link', { name: 'Workforce' }).click()
    await expect(page).toHaveURL('/workforce')
    await expect(page.getByRole('link', { name: 'Admin' })).toHaveCount(0)
  })
})
