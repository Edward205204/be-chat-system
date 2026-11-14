import { useState } from 'react'
import { Link, useNavigate } from '@tanstack/react-router'
import { EllipsisIcon, HashIcon, LockIcon, PlusIcon } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { useImageSrc } from '@/lib/files'
import type { ChannelResponse, ServerResponse } from '@/types/api'
import { ServerMenu } from '@/features/servers/components/server-menu'
import { useChannelsQuery, useDeleteChannelMutation } from '../queries'
import { ChannelFormDialog } from './channel-form-dialog'
import { ChannelPermissionsDialog } from './channel-permissions-dialog'

export function ChannelSidebar({ server }: { server: ServerResponse }) {
  const { data: channels, isPending } = useChannelsQuery(server.id)
  const [isCreateOpen, setCreateOpen] = useState(false)

  return (
    <aside className='flex w-60 shrink-0 flex-col border-r bg-sidebar'>
      <ServerBanner src={server.banner} />
      <ServerMenu server={server} />
      <div className='flex items-center justify-between px-4 pt-4 pb-1 text-xs font-semibold text-muted-foreground uppercase'>
        Channels
        {server.owner && (
          <button
            type='button'
            title='Tạo channel'
            className='cursor-pointer hover:text-foreground'
            onClick={() => setCreateOpen(true)}
          >
            <PlusIcon className='size-4' />
          </button>
        )}
      </div>
      <div className='flex-1 space-y-0.5 overflow-y-auto px-2'>
        {isPending &&
          Array.from({ length: 3 }, (_, i) => (
            <Skeleton key={i} className='h-8' />
          ))}
        {channels?.map((channel) => (
          <ChannelItem key={channel.id} server={server} channel={channel} />
        ))}
      </div>
      <ChannelFormDialog
        serverId={server.id}
        open={isCreateOpen}
        onOpenChange={setCreateOpen}
      />
    </aside>
  )
}

function ChannelItem({
  server,
  channel
}: {
  server: ServerResponse
  channel: ChannelResponse
}) {
  const navigate = useNavigate()
  const [dialog, setDialog] = useState<'rename' | 'permissions' | null>(null)
  const deleteChannel = useDeleteChannelMutation(server.id)
  const Icon = channel.private ? LockIcon : HashIcon

  const handleDelete = () => {
    if (!confirm(`Xoá channel #${channel.name}? Toàn bộ tin nhắn sẽ mất.`))
      return
    deleteChannel.mutate(channel.id, {
      onSuccess: () =>
        navigate({ to: '/servers/$serverId', params: { serverId: server.id } })
    })
  }

  return (
    <div className='group flex items-center rounded-md hover:bg-accent has-[[data-status=active]]:bg-accent'>
      <Link
        to='/servers/$serverId/$channelId'
        params={{ serverId: server.id, channelId: channel.id }}
        className='flex min-w-0 flex-1 items-center gap-1.5 px-2 py-1.5 text-sm text-muted-foreground data-[status=active]:font-medium data-[status=active]:text-foreground'
      >
        <Icon className='size-4 shrink-0' />
        <span className='truncate'>{channel.name}</span>
      </Link>
      {/* BE cho mọi member sửa/xoá channel public (bug #15) → chỉ hiện cho owner. */}
      {server.owner && (
        <DropdownMenu>
          <DropdownMenuTrigger className='mr-1 cursor-pointer rounded p-1 opacity-0 group-hover:opacity-100 hover:bg-background'>
            <EllipsisIcon className='size-4' />
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuItem onClick={() => setDialog('rename')}>
              Đổi tên
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setDialog('permissions')}>
              Quyền & thành viên
            </DropdownMenuItem>
            <DropdownMenuItem variant='destructive' onClick={handleDelete}>
              Xoá channel
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      )}
      <ChannelFormDialog
        serverId={server.id}
        channel={channel}
        open={dialog === 'rename'}
        onOpenChange={(o) => !o && setDialog(null)}
      />
      {server.owner && (
        <ChannelPermissionsDialog
          serverId={server.id}
          channel={channel}
          open={dialog === 'permissions'}
          onOpenChange={(o) => !o && setDialog(null)}
        />
      )}
    </div>
  )
}

function ServerBanner({ src }: { src: string | null }) {
  const imageSrc = useImageSrc(src)
  if (!imageSrc) return null
  return (
    <img src={imageSrc} alt='' className='h-24 w-full shrink-0 object-cover' />
  )
}
