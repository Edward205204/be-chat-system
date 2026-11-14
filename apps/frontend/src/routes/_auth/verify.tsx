import { createFileRoute } from '@tanstack/react-router'
import { VerifyOtpForm } from '@/features/auth/components/verify-otp-form'

export const Route = createFileRoute('/_auth/verify')({
  component: VerifyOtpForm
})
