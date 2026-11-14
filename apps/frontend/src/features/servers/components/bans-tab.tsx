import { LoadMoreList } from '@/components/common/load-more-list'
import { UserAvatar } from '@/components/common/user-avatar'
import { Button } from '@/components/ui/button'
import { formatDate } from '@/lib/date'
import { useBansQuery, useUnbanMutation } from '../queries'

export function BansTab({ serverId }: { serverId: string }) {
  const bans = useBansQuery(serverId)
  const unban = useUnbanMutation(serverId)

  return (
    <LoadMoreList
      height='lg'
      items={bans.data ?? []}
      isPending={bans.isPending}
      emptyTitle='Chưa ban ai'
      getKey={(ban) => ban.id}
      remote={{
        hasMore: bans.hasNextPage,
        isLoading: bans.isFetchingNextPage,
        onLoadMore: () => bans.fetchNextPage()
      }}
      renderItem={(ban) => (
        <div className='flex items-center gap-3 px-1 py-1'>
          <UserAvatar name={ban.user.displayName} src={ban.user.avatar} />
          <div className='min-w-0 flex-1'>
            <p className='truncate text-sm font-medium'>
              {ban.user.displayName}{' '}
              <span className='text-muted-foreground'>
                @{ban.user.username}
              </span>
            </p>
            <p className='truncate text-xs text-muted-foreground'>
              Bởi {ban.bannedBy.displayName} · {formatDate(ban.createdAt)}
              {ban.reason && ` · ${ban.reason}`}
            </p>
          </div>
          <Button
            size='sm'
            variant='outline'
            disabled={unban.isPending}
            onClick={() => unban.mutate(ban.user.id)}
          >
            Gỡ ban
          </Button>
        </div>
      )}
    />
  )
}
