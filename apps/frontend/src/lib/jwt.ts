import type { JwtPayload } from '@/types/api'

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(base64)) as JwtPayload
  } catch {
    return null
  }
}

/** Còn dưới `skewSeconds` giây là coi như hết hạn. */
export function isTokenExpired(token: string, skewSeconds = 15): boolean {
  const payload = decodeJwt(token)
  return !payload || payload.exp * 1000 < Date.now() + skewSeconds * 1000
}

/** Login trả token phẳng, verify/refresh trả token lồng `{ token }`. */
export const pickToken = (t: string | { token: string }) =>
  typeof t === 'string' ? t : t.token
