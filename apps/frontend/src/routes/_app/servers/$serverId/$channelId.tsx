import { createFileRoute } from '@tanstack/react-router'
import { EmptyState } from '@/components/common/empty-state'
import { useChannelsQuery } from '@/features/channels/queries'
import { ChatPanel } from '@/features/chat/components/chat-panel'

export const Route = createFileRoute('/_app/servers/$serverId/$channelId')({
  component: ChannelPage
})

function ChannelPage() {
  const { serverId, channelId } = Route.useParams()
  const { data: channels, isPending } = useChannelsQuery(serverId)
  // Chỉ mở channel có trong danh sách của mình (BE không kiểm VIEW_CHANNEL khi subscribe, bug #16).
  const channel = channels?.find((c) => c.id === channelId)

  if (isPending) return <div className='flex-1' />
  if (!channel) return <EmptyState title='Không tìm thấy channel' />
  return <ChatPanel key={channel.id} serverId={serverId} channel={channel} />
}
