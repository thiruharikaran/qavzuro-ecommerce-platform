/** Shared helpers for e2e specs. Uses a timestamp to keep test accounts unique across runs. */
export function uniqueEmail() {
  return `e2e-${Date.now()}-${Math.floor(Math.random() * 10000)}@qavzuro.dev`
}

export const TEST_PASSWORD = 'E2ETestPass123!'
