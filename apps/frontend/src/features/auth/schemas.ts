import { z } from 'zod'

// Quy tắc lấy đúng từ docs/api/01-auth.md mục 3 (BE trả validation lỗi thành 401/500).
const PASSWORD_REGEX =
  /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()\-+=_])(?=\S+$).{8,20}$/

export const emailSchema = z.email('Email không hợp lệ')
export const passwordSchema = z
  .string()
  .regex(
    PASSWORD_REGEX,
    '8-20 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt !@#$%^&*()-+=_'
  )
export const usernameSchema = z
  .string()
  .regex(/^[a-z0-9_]{3,20}$/, '3-20 ký tự: chữ thường, số, dấu _')
export const displayNameSchema = z
  .string()
  .trim()
  .min(1, 'Bắt buộc')
  .max(20, 'Tối đa 20 ký tự')
export const otpSchema = z.string().regex(/^\d{6}$/, 'OTP gồm 6 chữ số')

export const loginSchema = z.object({
  email: emailSchema,
  password: passwordSchema
})

export const registerSchema = z
  .object({
    email: emailSchema,
    username: usernameSchema,
    displayName: displayNameSchema,
    dateOfBirth: z.iso
      .date('Ngày không hợp lệ')
      .refine((v) => new Date(v) < new Date(), 'Phải là ngày trong quá khứ'),
    password: passwordSchema,
    confirmPassword: z.string()
  })
  .refine((v) => v.password === v.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Mật khẩu không khớp'
  })

export const verifyOtpSchema = z.object({ otp: otpSchema })

export const resetPasswordSchema = z.object({
  email: emailSchema,
  otp: otpSchema,
  password: passwordSchema
})

export type LoginValues = z.infer<typeof loginSchema>
export type RegisterValues = z.infer<typeof registerSchema>
export type VerifyOtpValues = z.infer<typeof verifyOtpSchema>
export type ResetPasswordValues = z.infer<typeof resetPasswordSchema>
