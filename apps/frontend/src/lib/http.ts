import axios, {
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig
} from 'axios'
import type { ApiResponse, TokenResponse } from '@/types/api'
import { pickToken } from '@/lib/jwt'
import { toApiError } from '@/lib/errors'
import { useSession } from '@/lib/session'

/** Path tương đối: đi qua proxy Vite (né CORS preflight 401, bug #1). */
export const API_BASE = '/rchat'

export const http = axios.create({ baseURL: API_BASE })

http.interceptors.request.use((config) => {
  const token = useSession.getState().accessToken
  if (token && !config.headers.Authorization)
    config.headers.Authorization = `Bearer ${token}`
  return config
})

let refreshing: Promise<string> | null = null

/** Refresh một lần cho mọi request đang chờ. Lỗi thì đăng xuất. */
export function refreshAccessToken(): Promise<string> {
  refreshing ??= doRefresh().finally(() => {
    refreshing = null
  })
  return refreshing
}

async function doRefresh(): Promise<string> {
  const { refreshToken, setTokens, clear } = useSession.getState()
  try {
    if (!refreshToken) throw new Error('No refresh token')
    const res = await axios.post<ApiResponse<TokenResponse>>(
      `${API_BASE}/auth/refresh`,
      { refreshToken }
    )
    const tokens = res.data.result!
    const access = pickToken(tokens.accessToken)
    setTokens(access, pickToken(tokens.refreshToken))
    return access
  } catch (error) {
    clear()
    window.location.assign('/login')
    throw error
  }
}

type RetryConfig = InternalAxiosRequestConfig & { _retried?: boolean }

http.interceptors.response.use(undefined, async (error) => {
  const config = error.config as RetryConfig | undefined
  const canRetry =
    error.response?.status === 401 &&
    config &&
    !config._retried &&
    !config.url?.startsWith('/auth/') &&
    useSession.getState().refreshToken
  if (!canRetry) throw toApiError(error)

  // BE trả validation lỗi thành 401 (bug #2): chỉ retry một lần rồi thôi.
  config._retried = true
  const token = await refreshAccessToken()
  config.headers.Authorization = `Bearer ${token}`
  return http(config)
})

/** Gọi API và trả `result` đã unwrap khỏi envelope. */
export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await http.request<ApiResponse<T>>(config)
  return res.data.result as T
}
