import { createFileRoute, Outlet, redirect } from '@tanstack/react-router'
import { isLoggedIn } from '@/lib/session'

export const Route = createFileRoute('/_auth')({
  beforeLoad: () => {
    if (isLoggedIn()) throw redirect({ to: '/' })
  },
  component: () => (
    <main className='flex min-h-svh items-center justify-center bg-muted p-4'>
      <Outlet />
    </main>
  )
})
