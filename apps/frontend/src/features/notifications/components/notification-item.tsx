import { useNavigate } from '@tanstack/react-router'
import { XIcon } from 'lucide-react'
import { toast } from 'sonner'
import { UserAvatar } from '@/components/common/user-avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatDate, formatMessageTime } from '@/lib/date'
import { toApiError } from '@/lib/errors'
import { useRespondInvitationMutation } from '@/features/servers/queries'
import type { InvitationNotification } from '../queries'
import { useDismissedInvitations } from '../store'

// Lỗi cho biết lời mời không còn dùng được → tự xoá thông báo.
const STALE_CODES = [
  'DIRECT_INVITE_NOT_FOUND',
  'DIRECT_INVITE_HAS_USED',
  'SERVER_NOT_EXIST',
  'USER_ALREADY_A_MEMBER'
]

const STATUS_LABEL = {
  ACCEPTED: 'Đã nhận',
  REJECTED: 'Đã từ chối',
  PENDING: 'Hết hạn'
} as const

export function NotificationItem({
  invitation
}: {
  invitation: InvitationNotification
}) {
  const navigate = useNavigate()
  const respond = useRespondInvitationMutation()
  const dismiss = useDismissedInvitations((s) => s.dismiss)
  const { server, inviter } = invitation

  const handleAccept = () =>
    respond.mutate(
      { id: invitation.id, action: 'ACCEPT' },
      {
        onSuccess: () => {
          toast.success(`Đã vào ${server.name}`)
          navigate({
            to: '/servers/$serverId',
            params: { serverId: server.id }
          })
        },
        onError: (err) =>
          STALE_CODES.includes(toApiError(err).code) && dismiss([invitation.id])
      }
    )

  return (
    <li className='flex h-16 items-center gap-3 px-4'>
      <UserAvatar name={server.name} src={server.avatar} className='size-10' />
      <div className='min-w-0 flex-1'>
        <p className='truncate text-sm'>
          <span className='font-medium'>{inviter.displayName}</span> mời bạn vào{' '}
          <span className='font-medium'>{server.name}</span>
        </p>
        <p className='truncate text-xs text-muted-foreground'>
          {formatMessageTime(invitation.createdAt)} · hết hạn{' '}
          {formatDate(invitation.expiresAt)}
        </p>
      </div>
      {invitation.isActionable ? (
        <>
          <Button size='sm' onClick={handleAccept} disabled={respond.isPending}>
            Nhận
          </Button>
          <Button
            size='sm'
            variant='ghost'
            disabled={respond.isPending}
            onClick={() =>
              respond.mutate({ id: invitation.id, action: 'REJECT' })
            }
          >
            Từ chối
          </Button>
        </>
      ) : (
        <>
          <Badge variant='secondary'>{STATUS_LABEL[invitation.status]}</Badge>
          <Button
            size='icon-sm'
            variant='ghost'
            aria-label='Xoá thông báo'
            onClick={() => dismiss([invitation.id])}
          >
            <XIcon />
          </Button>
        </>
      )}
    </li>
  )
}
