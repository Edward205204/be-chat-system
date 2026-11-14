import { useEffect } from 'react'
import { createFileRoute, Outlet, redirect } from '@tanstack/react-router'
import { ServerRail } from '@/features/servers/components/server-rail'
import { isLoggedIn } from '@/lib/session'
import { connectStomp, disconnectStomp } from '@/lib/stomp'

export const Route = createFileRoute('/_app')({
  beforeLoad: ({ location }) => {
    if (!isLoggedIn())
      throw redirect({ to: '/login', search: { redirect: location.href } })
  },
  component: AppLayout
})

function AppLayout() {
  useEffect(() => {
    connectStomp()
    return () => void disconnectStomp()
  }, [])

  return (
    <div className='flex h-svh overflow-hidden'>
      <ServerRail />
      <Outlet />
    </div>
  )
}
