import { createFileRoute, Outlet } from '@tanstack/react-router'
import { EmptyState } from '@/components/common/empty-state'
import { ChannelSidebar } from '@/features/channels/components/channel-sidebar'
import { MemberList } from '@/features/servers/components/member-list'
import { useServer } from '@/features/servers/queries'

export const Route = createFileRoute('/_app/servers/$serverId')({
  component: ServerLayout
})

function ServerLayout() {
  const { serverId } = Route.useParams()
  const { data: server, isPending } = useServer(serverId)

  if (isPending) return null
  if (!server)
    return (
      <EmptyState title='Không tìm thấy server'>
        Có thể bạn không còn là thành viên.
      </EmptyState>
    )

  return (
    <>
      <ChannelSidebar server={server} />
      <Outlet />
      <MemberList serverId={serverId} />
    </>
  )
}
