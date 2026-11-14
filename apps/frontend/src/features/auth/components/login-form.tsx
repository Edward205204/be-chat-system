import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { useLoginMutation } from '../queries'
import { loginSchema, type LoginValues } from '../schemas'
import { AuthCard } from './auth-card'

export function LoginForm({ redirectTo }: { redirectTo?: string }) {
  const navigate = useNavigate()
  const login = useLoginMutation()
  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' }
  })
  const { errors } = form.formState

  const handleSubmit = form.handleSubmit((values) =>
    login.mutate(values, {
      onSuccess: (result) => {
        if (result === 'unverified') return navigate({ to: '/verify' })
        if (redirectTo) return navigate({ href: redirectTo })
        navigate({ to: '/' })
      }
    })
  )

  return (
    <AuthCard
      title='Đăng nhập'
      description='Chào mừng quay lại RChat'
      footer={
        <span>
          Chưa có tài khoản?{' '}
          <Link to='/register' className='text-foreground underline'>
            Đăng ký
          </Link>
        </span>
      }
    >
      <form onSubmit={handleSubmit} className='grid gap-4'>
        <Field>
          <FieldLabel htmlFor='email'>Email</FieldLabel>
          <Input
            id='email'
            type='email'
            autoComplete='email'
            {...form.register('email')}
          />
          <FieldError>{errors.email?.message}</FieldError>
        </Field>
        <Field>
          <div className='flex items-center justify-between'>
            <FieldLabel htmlFor='password'>Mật khẩu</FieldLabel>
            <Link
              to='/forgot-password'
              className='text-xs text-muted-foreground underline'
            >
              Quên mật khẩu?
            </Link>
          </div>
          <Input
            id='password'
            type='password'
            autoComplete='current-password'
            {...form.register('password')}
          />
          <FieldError>{errors.password?.message}</FieldError>
        </Field>
        <Button type='submit' disabled={login.isPending}>
          Đăng nhập
        </Button>
      </form>
    </AuthCard>
  )
}
