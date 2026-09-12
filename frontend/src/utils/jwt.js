/** Decodes a JWT payload without verifying the signature (verification is the server's job). */
export function decodeJwtPayload(token) {
  try {
    const [, payload] = token.split('.')
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(json)
  } catch {
    return null
  }
}
