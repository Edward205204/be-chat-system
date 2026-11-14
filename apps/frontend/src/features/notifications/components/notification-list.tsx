import { useState } from 'react'
import { EmptyState } from '@/components/common/empty-state'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useInvitationNotifications } from '../queries'
import { useDismissedInvitations } from '../store'
import { NotificationItem } from './notification-item'

const PAGE_SIZE = 10

/** BE trả cả danh sách một lần → phân trang phía FE bằng "Tải thêm". */
export function NotificationList() {
  const { data: notifications = [], isPending } = useInvitationNotifications()
  const dismiss = useDismissedInvitations((s) => s.dismiss)
  const [visible, setVisible] = useState(PAGE_SIZE)
  const staleIds = notifications.filter((n) => !n.isActionable).map((n) => n.id)

  return (
    <div className='flex flex-col gap-3'>
      <div className='flex items-center justify-between'>
        <p className='text-sm text-muted-foreground'>
          {notifications.length} thông báo
        </p>
        <Button
          variant='outline'
          size='sm'
          disabled={staleIds.length === 0}
          onClick={() => dismiss(staleIds)}
        >
          Xoá thông báo hết hạn ({staleIds.length})
        </Button>
      </div>
      <div className='h-[min(32rem,65svh)] overflow-y-auto rounded-lg border'>
        {isPending && <Skeleton className='m-4 h-16' />}
        {!isPending && notifications.length === 0 && (
          <EmptyState title='Không có thông báo nào' />
        )}
        <ul className='divide-y'>
          {notifications.slice(0, visible).map((n) => (
            <NotificationItem key={n.id} invitation={n} />
          ))}
        </ul>
        {visible < notifications.length && (
          <div className='flex justify-center p-3'>
            <Button
              variant='ghost'
              size='sm'
              onClick={() => setVisible((v) => v + PAGE_SIZE)}
            >
              Tải thêm
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}
