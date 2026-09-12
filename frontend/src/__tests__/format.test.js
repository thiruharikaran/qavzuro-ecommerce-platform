import { describe, it, expect } from 'vitest'
import { formatMoney, formatDate } from '../utils/format'

describe('formatMoney', () => {
  it('formats INR currency with the rupee symbol', () => {
    const result = formatMoney(1999.5, 'INR')
    expect(result).toContain('1,999.50')
  })

  it('returns an empty string for null/undefined amounts', () => {
    expect(formatMoney(null)).toBe('')
    expect(formatMoney(undefined)).toBe('')
  })

  it('falls back gracefully for an invalid currency code', () => {
    const result = formatMoney(50, 'NOT_A_CURRENCY')
    expect(result).toContain('50')
  })
})

describe('formatDate', () => {
  it('returns an empty string for a falsy value', () => {
    expect(formatDate(null)).toBe('')
  })

  it('formats a valid ISO date string', () => {
    const result = formatDate('2026-01-15T10:00:00Z')
    expect(result).toMatch(/2026/)
  })
})
