import { createFileRoute, Navigate } from '@tanstack/react-router'
import { useSession } from '@/lib/session'

export const Route = createFileRoute('/_app/me')({ component: MeRoute })

/** Lối tắt tới hồ sơ của mình. */
function MeRoute() {
  const userId = useSession((s) => s.user?.id)
  if (!userId) return null
  return <Navigate to='/users/$userId' params={{ userId }} replace />
}
