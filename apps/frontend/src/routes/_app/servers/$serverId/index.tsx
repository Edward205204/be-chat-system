import { createFileRoute, Navigate } from '@tanstack/react-router'
import { EmptyState } from '@/components/common/empty-state'
import { useChannelsQuery } from '@/features/channels/queries'

export const Route = createFileRoute('/_app/servers/$serverId/')({
  component: ServerIndexPage
})

/** Vào server → mở channel đầu tiên (thường là `general`). */
function ServerIndexPage() {
  const { serverId } = Route.useParams()
  const { data: channels, isPending } = useChannelsQuery(serverId)
  const first = channels?.[0]

  if (isPending) return <div className='flex-1' />
  if (!first) return <EmptyState title='Server chưa có channel nào' />
  return (
    <Navigate
      to='/servers/$serverId/$channelId'
      params={{ serverId, channelId: first.id }}
      replace
    />
  )
}
