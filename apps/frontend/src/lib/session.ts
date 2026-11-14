import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserResponse } from '@/types/api'

interface SessionState {
  accessToken: string | null
  refreshToken: string | null
  user: UserResponse | null
  tmpToken: string | null
  setTokens: (accessToken: string, refreshToken: string) => void
  setUser: (user: UserResponse) => void
  setTmpToken: (tmpToken: string | null) => void
  clear: () => void
}

/** Phiên đăng nhập toàn cục. Là hạ tầng dùng chung nên đặt ở lib (http, stomp đọc trực tiếp). */
export const useSession = create<SessionState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      tmpToken: null,
      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken, tmpToken: null }),
      setUser: (user) => set({ user }),
      setTmpToken: (tmpToken) => set({ tmpToken }),
      clear: () =>
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          tmpToken: null
        })
    }),
    { name: 'rchat-session' }
  )
)

export const isLoggedIn = () => Boolean(useSession.getState().accessToken)
