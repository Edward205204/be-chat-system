import { HashIcon, LockIcon } from 'lucide-react'
import { useStompStatus } from '@/lib/stomp'
import { cn } from '@/lib/utils'
import type { ChannelResponse } from '@/types/api'
import { useMyMember } from '@/features/servers/queries'
import { useChannelRealtime } from '../queries'
import { MessageComposer } from './message-composer'
import { MessageList } from './message-list'

export function ChatPanel({
  serverId,
  channel
}: {
  serverId: string
  channel: ChannelResponse
}) {
  useChannelRealtime(serverId, channel.id)
  const status = useStompStatus((s) => s.status)
  const myMember = useMyMember(serverId)
  const Icon = channel.private ? LockIcon : HashIcon

  return (
    <section className='flex min-w-0 flex-1 flex-col'>
      <header className='flex h-12 shrink-0 items-center gap-2 border-b px-4'>
        <Icon className='size-5 text-muted-foreground' />
        <h1 className='font-semibold'>{channel.name}</h1>
        <span
          title={`Realtime: ${status}`}
          className={cn(
            'ml-auto size-2 rounded-full',
            status === 'connected' ? 'bg-green-500' : 'bg-amber-500'
          )}
        />
      </header>
      <MessageList channelId={channel.id} />
      <MessageComposer
        serverId={serverId}
        channelId={channel.id}
        channelName={channel.name}
        isMuted={Boolean(myMember?.muted)}
      />
    </section>
  )
}
