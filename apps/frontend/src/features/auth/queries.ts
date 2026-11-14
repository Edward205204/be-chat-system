import { useMutation, useQueryClient } from '@tanstack/react-query'
import { isUnverified } from '@/types/api'
import { pickToken } from '@/lib/jwt'
import { useSession } from '@/lib/session'
import { disconnectStomp } from '@/lib/stomp'
import { userApi } from '@/features/user/api'
import { authApi } from './api'
import type { LoginValues, RegisterValues } from './schemas'

/** Kết quả: 'ok' (đã đăng nhập) hoặc 'unverified' (cần OTP, tmpToken đã lưu). */
export function useLoginMutation() {
  return useMutation({
    mutationFn: async (values: LoginValues) => {
      const res = await authApi.login(values)
      const session = useSession.getState()
      if (isUnverified(res)) {
        session.setTmpToken(res.tmpToken)
        await authApi.sendEmailOtp(res.tmpToken).catch(() => undefined) // cooldown thì vẫn sang trang OTP
        return 'unverified' as const
      }
      session.setTokens(res.accessToken, res.refreshToken)
      session.setUser(res.user)
      return 'ok' as const
    }
  })
}

export function useRegisterMutation() {
  return useMutation({
    mutationFn: async ({ confirmPassword, ...values }: RegisterValues) => {
      const { tmpToken } = await authApi.register(values)
      useSession.getState().setTmpToken(tmpToken)
      await authApi.sendEmailOtp(tmpToken) // BE không tự gửi OTP
    }
  })
}

export function useSendOtpMutation() {
  return useMutation({
    mutationFn: (tmpToken: string) => authApi.sendEmailOtp(tmpToken)
  })
}

export function useVerifyOtpMutation() {
  return useMutation({
    mutationFn: async ({
      tmpToken,
      otp
    }: {
      tmpToken: string
      otp: string
    }) => {
      const tokens = await authApi.verifyEmailOtp(tmpToken, otp)
      const session = useSession.getState()
      session.setTokens(
        pickToken(tokens.accessToken),
        pickToken(tokens.refreshToken)
      )
      session.setUser(await userApi.getMe())
    }
  })
}

export function useForgotPasswordMutation() {
  return useMutation({
    mutationFn: (email: string) => authApi.forgotPassword({ email })
  })
}

export function useResetPasswordMutation() {
  return useMutation({ mutationFn: authApi.resetPassword })
}

export function useLogoutMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      const { refreshToken, clear } = useSession.getState()
      if (refreshToken)
        await authApi.logout(refreshToken).catch(() => undefined)
      await disconnectStomp()
      clear()
      queryClient.clear()
    }
  })
}
