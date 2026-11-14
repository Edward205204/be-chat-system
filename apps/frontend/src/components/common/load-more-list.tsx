import { useState, type ReactNode } from 'react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'
import { EmptyState } from './empty-state'

/** Số dòng hiện thêm mỗi lần khi cắt ở client. */
const LIST_PAGE_SIZE = 10

const LIST_HEIGHT = {
  sm: 'h-48',
  md: 'h-72',
  lg: 'h-96',
  fill: 'min-h-0 flex-1'
} as const

export interface RemotePaging {
  hasMore: boolean
  isLoading: boolean
  onLoadMore: () => void
}

interface LoadMoreListProps<T> {
  items: T[]
  getKey: (item: T) => string
  renderItem: (item: T) => ReactNode
  emptyTitle: string
  isPending?: boolean
  height?: keyof typeof LIST_HEIGHT
  pageSize?: number
  /** BE có phân trang: hiện hết `items` đã tải, nút "Tải thêm" gọi trang sau. Không có thì cắt ở client. */
  remote?: RemotePaging
  className?: string
}

/** Danh sách số lượng không cố định: chiều cao cố định, cuộn bên trong, nút "Tải thêm". */
export function LoadMoreList<T>({
  items,
  getKey,
  renderItem,
  emptyTitle,
  isPending,
  height = 'md',
  pageSize = LIST_PAGE_SIZE,
  remote,
  className
}: LoadMoreListProps<T>) {
  const [limit, setLimit] = useState(pageSize)
  const visible = remote ? items : items.slice(0, limit)
  const hasMore = remote ? remote.hasMore : items.length > limit

  const handleLoadMore = () =>
    remote ? remote.onLoadMore() : setLimit((n) => n + pageSize)

  return (
    <div
      className={cn(
        'flex flex-col overflow-y-auto rounded-md border [scrollbar-gutter:stable]',
        LIST_HEIGHT[height],
        className
      )}
    >
      {isPending ? (
        <div className='grid gap-2 p-2'>
          {Array.from({ length: 4 }, (_, i) => (
            <Skeleton key={i} className='h-10' />
          ))}
        </div>
      ) : items.length === 0 ? (
        <EmptyState title={emptyTitle} />
      ) : (
        <>
          <ul className='grid gap-1 p-2'>
            {visible.map((item) => (
              <li key={getKey(item)}>{renderItem(item)}</li>
            ))}
          </ul>
          {hasMore && (
            <div className='px-2 pb-2'>
              <Button
                variant='ghost'
                size='sm'
                className='w-full'
                disabled={remote?.isLoading}
                onClick={handleLoadMore}
              >
                {remote?.isLoading ? 'Đang tải…' : 'Tải thêm'}
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
