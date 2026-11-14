import { useEffect, useRef } from 'react'
import { UserAvatar } from '@/components/common/user-avatar'
import { EmptyState } from '@/components/common/empty-state'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { formatMessageTime, minutesBetween } from '@/lib/date'
import type { ChatMessage } from '@/types/api'
import { useMessagesQuery } from '../queries'

const GROUP_MINUTES = 5

export function MessageList({ channelId }: { channelId: string }) {
  const {
    data: messages = [],
    isPending,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage
  } = useMessagesQuery(channelId)
  const bottomRef = useRef<HTMLDivElement>(null)
  const lastId = messages.at(-1)?.id

  // Chỉ cuộn khi có tin mới ở cuối (tải tin cũ không đổi lastId).
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ block: 'end' })
  }, [lastId])

  if (isPending) {
    return (
      <div className='flex-1 space-y-4 p-4'>
        {Array.from({ length: 5 }, (_, i) => (
          <Skeleton key={i} className='h-12' />
        ))}
      </div>
    )
  }

  return (
    <div className='flex flex-1 flex-col overflow-y-auto py-4'>
      {hasNextPage && (
        <Button
          variant='ghost'
          size='sm'
          className='mx-auto'
          onClick={() => fetchNextPage()}
          disabled={isFetchingNextPage}
        >
          {isFetchingNextPage ? 'Đang tải…' : 'Tải tin nhắn cũ hơn'}
        </Button>
      )}
      {messages.length === 0 && (
        <EmptyState title='Chưa có tin nhắn'>
          Hãy là người mở đầu cuộc trò chuyện.
        </EmptyState>
      )}
      {messages.map((message, i) => (
        <MessageItem
          key={message.id}
          message={message}
          isGrouped={isSameGroup(messages[i - 1], message)}
        />
      ))}
      <div ref={bottomRef} />
    </div>
  )
}

const isSameGroup = (prev: ChatMessage | undefined, cur: ChatMessage) =>
  Boolean(prev) &&
  prev!.senderId === cur.senderId &&
  minutesBetween(prev!.createdAt, cur.createdAt) < GROUP_MINUTES

function MessageItem({
  message,
  isGrouped
}: {
  message: ChatMessage
  isGrouped: boolean
}) {
  if (isGrouped) {
    return (
      <p className='px-4 py-0.5 pl-16 text-sm break-words whitespace-pre-wrap hover:bg-accent/50'>
        {message.content}
      </p>
    )
  }
  return (
    <div className='mt-3 flex gap-3 px-4 py-0.5 hover:bg-accent/50'>
      <UserAvatar
        name={message.senderName}
        src={message.senderAvatar}
        className='size-9'
      />
      <div className='min-w-0'>
        <p className='flex items-baseline gap-2'>
          {/* senderName là username (06-chat 1) */}
          <span className='text-sm font-semibold'>{message.senderName}</span>
          <span className='text-xs text-muted-foreground'>
            {formatMessageTime(message.createdAt)}
          </span>
        </p>
        <p className='text-sm break-words whitespace-pre-wrap'>
          {message.content}
        </p>
      </div>
    </div>
  )
}
