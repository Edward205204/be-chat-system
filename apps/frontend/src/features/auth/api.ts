import { request } from '@/lib/http'
import type {
  ForgotPasswordRequest,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ResetPasswordRequest,
  TokenResponse,
  UnverifiedResponse
} from '@/types/api'

const bearer = (token: string) => ({ Authorization: `Bearer ${token}` })

export const authApi = {
  register: (body: RegisterRequest) =>
    request<UnverifiedResponse>({
      method: 'POST',
      url: '/auth/register',
      data: body
    }),
  login: (body: LoginRequest) =>
    request<LoginResponse>({ method: 'POST', url: '/auth/login', data: body }),
  sendEmailOtp: (tmpToken: string) =>
    request<void>({
      method: 'POST',
      url: '/auth/send/email-otp',
      headers: bearer(tmpToken)
    }),
  verifyEmailOtp: (tmpToken: string, otp: string) =>
    request<TokenResponse>({
      method: 'POST',
      url: '/auth/verify/email-otp',
      data: { otp },
      headers: bearer(tmpToken)
    }),
  logout: (refreshToken: string) =>
    request<void>({
      method: 'POST',
      url: '/auth/logout',
      data: { refreshToken }
    }),
  forgotPassword: (body: ForgotPasswordRequest) =>
    request<void>({
      method: 'POST',
      url: '/auth/send/forgot-password',
      data: body
    }),
  resetPassword: (body: ResetPasswordRequest) =>
    request<void>({ method: 'POST', url: '/auth/reset-password', data: body })
}
