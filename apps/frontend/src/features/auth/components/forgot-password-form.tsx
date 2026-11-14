import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { useForgotPasswordMutation, useResetPasswordMutation } from '../queries'
import {
  emailSchema,
  resetPasswordSchema,
  type ResetPasswordValues
} from '../schemas'
import { AuthCard } from './auth-card'

/** Bước 1: gửi OTP về email. Bước 2: nhập OTP + mật khẩu mới. */
export function ForgotPasswordForm() {
  const navigate = useNavigate()
  const [isOtpSent, setOtpSent] = useState(false)
  const forgot = useForgotPasswordMutation()
  const reset = useResetPasswordMutation()
  const form = useForm<ResetPasswordValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { email: '', otp: '', password: '' }
  })
  const { errors } = form.formState

  const handleSendOtp = async () => {
    const email = form.getValues('email')
    if (!emailSchema.safeParse(email).success)
      return form.setError('email', { message: 'Email không hợp lệ' })
    forgot.mutate(email, { onSuccess: () => setOtpSent(true) })
  }

  const handleSubmit = form.handleSubmit((values) =>
    reset.mutate(values, {
      onSuccess: () => {
        toast.success('Đã đặt lại mật khẩu, hãy đăng nhập')
        navigate({ to: '/login' })
      }
    })
  )

  return (
    <AuthCard
      title='Quên mật khẩu'
      description={
        isOtpSent
          ? 'Nếu email tồn tại, mã OTP đã được gửi.'
          : 'Nhập email để nhận mã OTP.'
      }
      footer={
        <Link to='/login' className='underline'>
          Về đăng nhập
        </Link>
      }
    >
      <form onSubmit={handleSubmit} className='grid gap-4'>
        <Field>
          <FieldLabel htmlFor='email'>Email</FieldLabel>
          <Input
            id='email'
            type='email'
            readOnly={isOtpSent}
            {...form.register('email')}
          />
          <FieldError>{errors.email?.message}</FieldError>
        </Field>
        {isOtpSent ? (
          <>
            <Field>
              <FieldLabel htmlFor='otp'>Mã OTP</FieldLabel>
              <Input
                id='otp'
                inputMode='numeric'
                maxLength={6}
                {...form.register('otp')}
              />
              <FieldError>{errors.otp?.message}</FieldError>
            </Field>
            <Field>
              <FieldLabel htmlFor='password'>Mật khẩu mới</FieldLabel>
              <Input
                id='password'
                type='password'
                {...form.register('password')}
              />
              <FieldError>{errors.password?.message}</FieldError>
            </Field>
            <Button type='submit' disabled={reset.isPending}>
              Đặt lại mật khẩu
            </Button>
          </>
        ) : (
          <Button
            type='button'
            onClick={handleSendOtp}
            disabled={forgot.isPending}
          >
            Gửi mã OTP
          </Button>
        )}
      </form>
    </AuthCard>
  )
}
