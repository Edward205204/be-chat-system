import { useState, type FormEvent } from 'react'
import { CopyIcon } from 'lucide-react'
import { toast } from 'sonner'
import { UserAvatar } from '@/components/common/user-avatar'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Separator } from '@/components/ui/separator'
import { useSearchUserMutation } from '@/features/user/queries'
import { buildInviteUrl } from '../api'
import {
  useCancelInvitationMutation,
  useCreateInviteLinkMutation,
  useInviteUserMutation
} from '../queries'

interface InviteDialogProps {
  serverId: string
  open: boolean
  onOpenChange: (open: boolean) => void
}

/** Hai cách mời: link mời (ai có link đều vào được) và mời trực tiếp theo username/email. */
export function InviteDialog({
  serverId,
  open,
  onOpenChange
}: InviteDialogProps) {
  const createLink = useCreateInviteLinkMutation(serverId)
  const searchUser = useSearchUserMutation()
  const inviteUser = useInviteUserMutation(serverId)
  const cancelInvite = useCancelInvitationMutation(serverId)
  const sentInvite =
    inviteUser.data?.invitee.id === searchUser.data?.id
      ? inviteUser.data
      : undefined
  const [query, setQuery] = useState('')
  const inviteUrl = createLink.data ? buildInviteUrl(createLink.data.token) : ''
  const found = searchUser.data

  const handleCopy = async () => {
    await navigator.clipboard.writeText(inviteUrl)
    toast.success('Đã copy link mời')
  }

  const handleSearch = (e: FormEvent) => {
    e.preventDefault()
    const q = query.trim()
    if (q) searchUser.mutate(q)
  }

  const handleCancel = () => {
    if (!sentInvite) return
    cancelInvite.mutate(sentInvite.id, {
      onSuccess: () => {
        toast.success('Đã huỷ lời mời')
        inviteUser.reset()
      }
    })
  }

  const handleInvite = () => {
    if (!found) return
    inviteUser.mutate(found.id, {
      onSuccess: () => toast.success(`Đã gửi lời mời tới ${found.displayName}`)
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Mời bạn bè</DialogTitle>
          <DialogDescription>Link mời có hạn 3 ngày.</DialogDescription>
        </DialogHeader>

        {inviteUrl ? (
          <div className='flex gap-2'>
            <Input readOnly value={inviteUrl} />
            <Button
              size='icon'
              variant='outline'
              onClick={handleCopy}
              aria-label='Copy'
            >
              <CopyIcon />
            </Button>
          </div>
        ) : (
          <Button
            variant='outline'
            onClick={() => createLink.mutate()}
            disabled={createLink.isPending}
          >
            Tạo link mời
          </Button>
        )}

        <Separator />

        <form onSubmit={handleSearch} className='flex gap-2'>
          <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder='Username hoặc email chính xác'
          />
          <Button
            type='submit'
            variant='secondary'
            disabled={searchUser.isPending}
          >
            Tìm
          </Button>
        </form>
        {/* Giữ chỗ sẵn cho kết quả tìm để popup không đổi chiều cao. */}
        <div className='flex min-h-10 items-center'>
          {searchUser.isSuccess && !found && (
            <p className='text-sm text-muted-foreground'>
              Không tìm thấy người dùng.
            </p>
          )}
          {found && (
            <div className='flex w-full items-center gap-3'>
              <UserAvatar name={found.displayName} src={found.avatar} />
              <div className='min-w-0 flex-1'>
                <p className='truncate text-sm font-medium'>
                  {found.displayName}
                </p>
                <p className='truncate text-xs text-muted-foreground'>
                  @{found.username}
                </p>
              </div>
              {sentInvite ? (
                <Button
                  size='sm'
                  variant='outline'
                  onClick={handleCancel}
                  disabled={cancelInvite.isPending}
                >
                  Huỷ lời mời
                </Button>
              ) : (
                <Button
                  size='sm'
                  onClick={handleInvite}
                  disabled={inviteUser.isPending}
                >
                  Mời
                </Button>
              )}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}
