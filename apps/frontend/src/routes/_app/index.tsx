import { createFileRoute } from '@tanstack/react-router'
import { EmptyState } from '@/components/common/empty-state'
import { InvitationList } from '@/features/servers/components/invitation-list'
import { useSession } from '@/lib/session'

export const Route = createFileRoute('/_app/')({ component: HomePage })

function HomePage() {
  const displayName = useSession((s) => s.user?.displayName)
  return (
    <main className='flex flex-1 flex-col items-center gap-6 overflow-y-auto p-8'>
      <EmptyState title={`Xin chào ${displayName ?? ''} 👋`}>
        Chọn một server bên trái, tạo server mới (+) hoặc tham gia bằng link
        mời.
      </EmptyState>
      <InvitationList />
    </main>
  )
}
