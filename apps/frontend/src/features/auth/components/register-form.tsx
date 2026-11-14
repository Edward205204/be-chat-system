import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Link, useNavigate } from '@tanstack/react-router'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { useRegisterMutation } from '../queries'
import { registerSchema, type RegisterValues } from '../schemas'
import { AuthCard } from './auth-card'

const FIELDS: { name: keyof RegisterValues; label: string; type?: string }[] = [
  { name: 'email', label: 'Email', type: 'email' },
  { name: 'username', label: 'Username' },
  { name: 'displayName', label: 'Tên hiển thị' },
  { name: 'dateOfBirth', label: 'Ngày sinh', type: 'date' },
  { name: 'password', label: 'Mật khẩu', type: 'password' },
  { name: 'confirmPassword', label: 'Nhập lại mật khẩu', type: 'password' }
]

export function RegisterForm() {
  const navigate = useNavigate()
  const register = useRegisterMutation()
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: '',
      username: '',
      displayName: '',
      dateOfBirth: '',
      password: '',
      confirmPassword: ''
    }
  })
  const { errors } = form.formState

  const handleSubmit = form.handleSubmit((values) =>
    register.mutate(values, { onSuccess: () => navigate({ to: '/verify' }) })
  )

  return (
    <AuthCard
      title='Tạo tài khoản'
      footer={
        <span>
          Đã có tài khoản?{' '}
          <Link to='/login' className='text-foreground underline'>
            Đăng nhập
          </Link>
        </span>
      }
    >
      <form onSubmit={handleSubmit} className='grid gap-4'>
        {FIELDS.map(({ name, label, type }) => (
          <Field key={name}>
            <FieldLabel htmlFor={name}>{label}</FieldLabel>
            <Input id={name} type={type ?? 'text'} {...form.register(name)} />
            <FieldError>{errors[name]?.message}</FieldError>
          </Field>
        ))}
        <Button type='submit' disabled={register.isPending}>
          Đăng ký
        </Button>
      </form>
    </AuthCard>
  )
}
