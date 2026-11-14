import { createFileRoute } from '@tanstack/react-router'
import { z } from 'zod'
import { LoginForm } from '@/features/auth/components/login-form'

export const Route = createFileRoute('/_auth/login')({
  validateSearch: z.object({ redirect: z.string().optional() }),
  component: LoginPage
})

function LoginPage() {
  const { redirect } = Route.useSearch()
  return <LoginForm redirectTo={redirect} />
}
