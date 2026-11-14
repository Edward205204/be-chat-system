import { CopyIcon } from 'lucide-react'
import { toast } from 'sonner'
import { LoadMoreList } from '@/components/common/load-more-list'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatDate, isFuture } from '@/lib/date'
import { buildInviteUrl } from '../api'
import {
  useCreateInviteLinkMutation,
  useInviteLinksQuery,
  useRevokeInviteLinkMutation
} from '../queries'

export function InviteLinksTab({ serverId }: { serverId: string }) {
  const links = useInviteLinksQuery(serverId)
  const createLink = useCreateInviteLinkMutation(serverId)
  const revoke = useRevokeInviteLinkMutation(serverId)

  const handleCopy = async (token: string) => {
    await navigator.clipboard.writeText(buildInviteUrl(token))
    toast.success('Đã copy link mời')
  }

  return (
    <div className='grid gap-3'>
      <Button
        variant='secondary'
        className='w-fit'
        onClick={() => createLink.mutate()}
        disabled={createLink.isPending}
      >
        Tạo link mới
      </Button>
      <LoadMoreList
        items={links.data ?? []}
        isPending={links.isPending}
        emptyTitle='Chưa có link mời'
        getKey={(link) => link.id}
        remote={{
          hasMore: links.hasNextPage,
          isLoading: links.isFetchingNextPage,
          onLoadMore: () => links.fetchNextPage()
        }}
        renderItem={(link) => (
          <div className='flex items-center gap-3 px-1 py-1'>
            <div className='min-w-0 flex-1'>
              <p className='flex items-center gap-2 truncate font-mono text-sm'>
                {link.token}
                {!isFuture(link.expiresAt) && (
                  <Badge variant='secondary'>Hết hạn</Badge>
                )}
              </p>
              <p className='truncate text-xs text-muted-foreground'>
                {link.createdBy.displayName} · {link.useCount} lượt dùng · hạn{' '}
                {formatDate(link.expiresAt)}
              </p>
            </div>
            <Button
              size='icon-sm'
              variant='ghost'
              aria-label='Copy link'
              onClick={() => handleCopy(link.token)}
            >
              <CopyIcon />
            </Button>
            <Button
              size='sm'
              variant='outline'
              disabled={revoke.isPending}
              onClick={() =>
                confirm('Thu hồi link này?') && revoke.mutate(link.id)
              }
            >
              Thu hồi
            </Button>
          </div>
        )}
      />
    </div>
  )
}
