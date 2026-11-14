import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { decodeJwt } from '@/lib/jwt'
import { useSession } from '@/lib/session'
import { useSendOtpMutation, useVerifyOtpMutation } from '../queries'
import { verifyOtpSchema, type VerifyOtpValues } from '../schemas'
import { AuthCard } from './auth-card'

/** BE không báo còn bao lâu (bug #19) → FE tự đếm 120s. */
const OTP_COOLDOWN_SECONDS = 120

export function VerifyOtpForm() {
  const navigate = useNavigate()
  const tmpToken = useSession((s) => s.tmpToken)
  const verifyOtp = useVerifyOtpMutation()
  const sendOtp = useSendOtpMutation()
  const [cooldown, setCooldown] = useState(OTP_COOLDOWN_SECONDS)
  const form = useForm<VerifyOtpValues>({
    resolver: zodResolver(verifyOtpSchema),
    defaultValues: { otp: '' }
  })

  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setTimeout(() => setCooldown((c) => c - 1), 1000)
    return () => clearTimeout(timer)
  }, [cooldown])

  if (!tmpToken) {
    return (
      <AuthCard
        title='Xác thực email'
        description='Phiên xác thực không còn. Hãy đăng nhập lại.'
      >
        <Button className='w-full' render={<Link to='/login' />}>
          Về đăng nhập
        </Button>
      </AuthCard>
    )
  }

  const email = decodeJwt(tmpToken)?.email

  const handleSubmit = form.handleSubmit(({ otp }) =>
    verifyOtp.mutate(
      { tmpToken, otp },
      { onSuccess: () => navigate({ to: '/' }) }
    )
  )

  const handleResend = () =>
    sendOtp.mutate(tmpToken, {
      onSuccess: () => {
        toast.success('Đã gửi lại mã')
        setCooldown(OTP_COOLDOWN_SECONDS)
      }
    })

  return (
    <AuthCard
      title='Xác thực email'
      description={`Nhập mã 6 số đã gửi tới ${email ?? 'email của bạn'}.`}
    >
      <form onSubmit={handleSubmit} className='grid gap-4'>
        <Field>
          <FieldLabel htmlFor='otp'>Mã OTP</FieldLabel>
          <Input
            id='otp'
            inputMode='numeric'
            maxLength={6}
            autoFocus
            autoComplete='one-time-code'
            {...form.register('otp')}
          />
          <FieldError>{form.formState.errors.otp?.message}</FieldError>
        </Field>
        <Button type='submit' disabled={verifyOtp.isPending}>
          Xác thực
        </Button>
        <Button
          type='button'
          variant='ghost'
          onClick={handleResend}
          disabled={cooldown > 0 || sendOtp.isPending}
        >
          {cooldown > 0 ? `Gửi lại mã sau ${cooldown}s` : 'Gửi lại mã'}
        </Button>
      </form>
    </AuthCard>
  )
}
