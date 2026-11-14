import { useState } from 'react'
import { LoadMoreList } from '@/components/common/load-more-list'
import { PermissionToggles } from '@/components/common/permission-toggles'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { NativeSelect } from '@/components/ui/native-select'
import { Skeleton } from '@/components/ui/skeleton'
import { CHANNEL_PERMISSIONS, EVERYONE_ROLE } from '@/features/roles/constants'
import { useRolesQuery } from '@/features/roles/queries'
import { useMembersQuery } from '@/features/servers/queries'
import type { ChannelResponse } from '@/types/api'
import {
  useChannelPermissionMutation,
  useChannelPermissionsQuery,
  useInviteToChannelMutation
} from '../queries'

interface ChannelPermissionsDialogProps {
  serverId: string
  channel: ChannelResponse
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ChannelPermissionsDialog({
  serverId,
  channel,
  open,
  onOpenChange
}: ChannelPermissionsDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent size='lg'>
        <DialogHeader>
          <DialogTitle>Quyền của #{channel.name}</DialogTitle>
          <DialogDescription>
            {channel.private
              ? 'Channel riêng tư: chỉ owner và người được cấp quyền mới thấy.'
              : 'Channel public: mọi member đã có mọi quyền, bảng dưới chỉ có tác dụng ở channel riêng tư.'}
          </DialogDescription>
        </DialogHeader>
        <DialogBody>
          {open && (
            <PermissionEditor serverId={serverId} channelId={channel.id} />
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

function PermissionEditor({
  serverId,
  channelId
}: {
  serverId: string
  channelId: string
}) {
  const { data: config, isPending } = useChannelPermissionsQuery(
    serverId,
    channelId
  )
  const { data: roles = [] } = useRolesQuery(serverId)
  const { data: members } = useMembersQuery(serverId)
  const change = useChannelPermissionMutation(serverId, channelId)
  const invite = useInviteToChannelMutation(serverId, channelId)
  const [selected, setSelected] = useState('')

  if (isPending || !config) return <Skeleton className='h-full' />

  // Quyền channel gán cho @everyone không có tác dụng (05 mục 5.3) → bỏ khỏi danh sách.
  const assignableRoles = roles.filter((r) => r.name !== EVERYONE_ROLE)
  const candidates =
    members?.items.filter(
      (m) =>
        !m.owner &&
        !config.userPermissions.some((u) => u.memberId === m.memberId)
    ) ?? []

  const handleInvite = () => {
    if (selected) invite.mutate(selected, { onSuccess: () => setSelected('') })
  }

  return (
    <div className='grid gap-5'>
      <section className='grid gap-2'>
        <h3 className='text-sm font-semibold'>Thêm member vào channel</h3>
        <div className='flex gap-2'>
          <NativeSelect
            value={selected}
            onChange={(e) => setSelected(e.target.value)}
          >
            <option value=''>Chọn member…</option>
            {candidates.map((m) => (
              <option key={m.memberId} value={m.memberId}>
                {m.displayName} (@{m.username})
              </option>
            ))}
          </NativeSelect>
          <Button
            variant='secondary'
            onClick={handleInvite}
            disabled={!selected || invite.isPending}
          >
            Thêm
          </Button>
        </div>
      </section>

      <section className='grid gap-3'>
        <h3 className='text-sm font-semibold'>Theo member</h3>
        <LoadMoreList
          items={config.userPermissions}
          emptyTitle='Chưa cấp cho member nào'
          getKey={(u) => u.memberId}
          renderItem={(u) => (
            <div className='grid gap-1.5 p-1'>
              <p className='text-sm font-medium'>{u.displayName}</p>
              <PermissionToggles
                items={CHANNEL_PERMISSIONS}
                active={u.permissions}
                disabled={change.isPending}
                onToggle={(permission, enabled) =>
                  change.mutate({
                    target: 'member',
                    id: u.memberId,
                    permission,
                    enabled
                  })
                }
              />
            </div>
          )}
        />
      </section>

      <section className='grid gap-3'>
        <h3 className='text-sm font-semibold'>Theo role</h3>
        <LoadMoreList
          items={assignableRoles}
          emptyTitle='Chưa có role nào. Tạo role trong Cài đặt server.'
          getKey={(role) => role.id}
          renderItem={(role) => (
            <div className='grid gap-1.5 p-1'>
              <p className='flex items-center gap-2 text-sm font-medium'>
                <span
                  className='size-3 rounded-full'
                  style={{ backgroundColor: role.color }}
                />
                {role.name}
              </p>
              <PermissionToggles
                items={CHANNEL_PERMISSIONS}
                active={
                  config.rolePermissions.find((r) => r.roleId === role.id)
                    ?.permissions ?? []
                }
                disabled={change.isPending}
                onToggle={(permission, enabled) =>
                  change.mutate({
                    target: 'role',
                    id: role.id,
                    permission,
                    enabled
                  })
                }
              />
            </div>
          )}
        />
      </section>
    </div>
  )
}
