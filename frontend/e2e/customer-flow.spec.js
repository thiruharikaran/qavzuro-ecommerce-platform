import { test, expect } from '@playwright/test'
import { uniqueEmail, TEST_PASSWORD } from './helpers.js'

/**
 * End-to-end coverage of the critical customer journey:
 * register -> browse -> search -> filter/sort -> product detail -> variant
 * -> add to cart -> update cart -> checkout -> test payment -> confirmation
 * -> order history.
 *
 * Requires the backend running at its configured URL with dev seed data
 * enabled (app.seed.enabled=true) and the frontend dev server running.
 */
test.describe('Customer purchase journey', () => {
  test('registers, shops, checks out, and sees the order in history', async ({ page }) => {
    const email = uniqueEmail()

    await page.goto('/register')
    await page.getByLabel('First name').fill('E2E')
    await page.getByLabel('Last name').fill('Tester')
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password', { exact: true }).fill(TEST_PASSWORD)
    await page.getByRole('button', { name: /create account/i }).click()

    await expect(page).toHaveURL('/')

    // Browse products
    await page.getByRole('link', { name: 'Shop' }).click()
    await expect(page).toHaveURL(/\/products/)

    // Search
    await page.getByPlaceholder(/search products/i).first().fill('Qavzuro')
    await page.getByRole('button', { name: 'Search' }).first().click()
    await expect(page).toHaveURL(/q=Qavzuro/)

    // Sort
    await page.getByLabel('Sort products').selectOption('price_asc')

    // Open first product
    const firstProductLink = page.locator('main a[href^="/products/"]').first()
    await firstProductLink.click()
    await expect(page.locator('h1')).toBeVisible()

    // Select a variant if present (best-effort; not every product has variants)
    const variantButtons = page.locator('button', { hasText: '/' })
    if (await variantButtons.count() > 0) {
      await variantButtons.first().click()
    }

    // Add to cart
    await page.getByRole('button', { name: /add to cart/i }).click()

    // Go to cart and update quantity
    await page.goto('/cart')
    const qtyInput = page.locator('input[type="number"]').first()
    await qtyInput.fill('2')
    await qtyInput.blur();

    // Proceed to checkout
    await page.getByRole('button', { name: /proceed to checkout/i }).click()
    await expect(page).toHaveURL(/\/checkout/)

    // Fill a new address
    const addAddressBtn = page.getByRole('button', { name: /add a new address/i });
    if (await addAddressBtn.isVisible()) {
      await addAddressBtn.click()
      await page.getByPlaceholder('fullName').fill('E2E Tester')
      await page.getByPlaceholder('phone').fill('9999999999')
      await page.getByPlaceholder('line1').fill('123 Test Street')
      await page.getByPlaceholder('city').fill('Chennai')
      await page.getByPlaceholder('state').fill('Tamil Nadu')
      await page.getByPlaceholder('postalCode').fill('600001')
      await page.getByPlaceholder('country').fill('India')
      await page.getByRole('button', { name: /save address/i }).click()
    }

    // Choose the guaranteed-success sandbox payment method and place the order
    await page.getByLabel(/payment \(sandbox\)/i).selectOption('TEST_CARD').catch(() => {})
    await page.getByRole('button', { name: /place order/i }).click()

    // Confirmation page
    await expect(page).toHaveURL(/\/orders\/.+\/confirmation/, { timeout: 15000 })
    await expect(page.getByText(/order confirmed/i)).toBeVisible()

    // Order history shows the new order
    await page.getByRole('link', { name: /view your orders/i }).click()
    await expect(page).toHaveURL(/\/account\/orders/)
    await expect(page.locator('main')).toContainText('QVZ-')
  })

  test('declined test payment surfaces a clear error and does not create a confirmed order', async ({ page }) => {
    const email = uniqueEmail()
    await page.goto('/register')
    await page.getByLabel('First name').fill('E2E')
    await page.getByLabel('Last name').fill('Decline')
    await page.getByLabel('Email').fill(email)
    await page.getByLabel('Password', { exact: true }).fill(TEST_PASSWORD)
    await page.getByRole('button', { name: /create account/i }).click()

    await page.goto('/products')
    const firstProductLink = page.locator('main a[href^="/products/"]').first()
    await firstProductLink.click()
    await page.getByRole('button', { name: /add to cart/i }).click()

    await page.goto('/checkout')
    const addAddressBtn = page.getByRole('button', { name: /add a new address/i })
    if (await addAddressBtn.isVisible()) {
      await addAddressBtn.click()
      await page.getByPlaceholder('fullName').fill('E2E Decline')
      await page.getByPlaceholder('phone').fill('9999999999')
      await page.getByPlaceholder('line1').fill('123 Test Street')
      await page.getByPlaceholder('city').fill('Chennai')
      await page.getByPlaceholder('state').fill('Tamil Nadu')
      await page.getByPlaceholder('postalCode').fill('600001')
      await page.getByPlaceholder('country').fill('India')
      await page.getByRole('button', { name: /save address/i }).click()
    }

    await page.getByLabel(/payment \(sandbox\)/i).selectOption('TEST_CARD_DECLINE')
    await page.getByRole('button', { name: /place order/i }).click()

    await expect(page.getByRole('alert')).toContainText(/payment failed/i)
    await expect(page).toHaveURL(/\/checkout/)
  })
})
