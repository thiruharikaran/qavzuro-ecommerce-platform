import { describe, it, expect } from 'vitest'
import { decodeJwtPayload } from '../utils/jwt'

function makeFakeJwt(payload) {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = btoa(JSON.stringify(payload))
  return `${header}.${body}.fake-signature`
}

describe('decodeJwtPayload', () => {
  it('decodes a well-formed JWT payload', () => {
    const token = makeFakeJwt({ sub: 'user-1', permissions: 'PRODUCT_VIEW,ORDER_VIEW' })
    const payload = decodeJwtPayload(token)
    expect(payload.sub).toBe('user-1')
    expect(payload.permissions).toBe('PRODUCT_VIEW,ORDER_VIEW')
  })

  it('returns null for malformed input instead of throwing', () => {
    expect(decodeJwtPayload('not-a-jwt')).toBeNull()
    expect(decodeJwtPayload('')).toBeNull()
  })
})
