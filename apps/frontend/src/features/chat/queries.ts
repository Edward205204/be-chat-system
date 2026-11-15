import { useEffect, useEffectEvent } from 'react'
import {
  useInfiniteQuery,
  useQueryClient,
  type InfiniteData,
  type QueryClient
} from '@tanstack/react-query'
import {
  STOMP,
  type ChatMessage,
  type CursorPage,
  type ServerMemberResponse
} from '@/types/api'
import {
  addStompErrorListener,
  publishJson,
  subscribeJson,
  useStompStatus
} from '@/lib/stomp'
import { useSession } from '@/lib/session'
import { serverKeys } from '@/features/servers/queries'
import { chatApi } from './api'

type MessagePages = InfiniteData<CursorPage<ChatMessage>, string | undefined>

const PAGE_SIZE = 30

export const chatKeys = {
  messages: (channelId: string) => ['chat', channelId, 'messages'] as const
}

export function useMessagesQuery(channelId: string) {
  return useInfiniteQuery({
    queryKey: chatKeys.messages(channelId),
    queryFn: ({ pageParam }) =>
      chatApi.messages(channelId, { cursor: pageParam, size: PAGE_SIZE }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (last) =>
      last.hasNext ? (last.nextCursor ?? undefined) : undefined,
    // pages[0] mới nhất → đảo lại để render cũ → mới.
    select: (data) => data.pages.toReversed().flatMap((page) => page.data),
    staleTime: Infinity // tin mới đến qua STOMP
  })
}

/** Gộp tin realtime vào trang mới nhất, khử trùng theo id (tin có thể trùng trang vừa tải). */
function appendMessage(
  data: MessagePages | undefined,
  message: ChatMessage
): MessagePages | undefined {
  if (!data || data.pages.some((p) => p.data.some((m) => m.id === message.id)))
    return data
  const [newest, ...rest] = data.pages
  return {
    ...data,
    pages: [{ ...newest, data: [...newest.data, message] }, ...rest]
  }
}

export const MUTED_ERROR_CODE = 'MEMBER_IS_MUTED'

/** Sửa cờ `muted` của mình ngay trong cache member; lần refetch member sau sẽ ghi đè bằng dữ liệu BE. */
function setMyMuted(
  queryClient: QueryClient,
  serverId: string,
  muted: boolean
) {
  const userId = useSession.getState().user?.id
  queryClient.setQueryData<ServerMemberResponse>(
    serverKeys.members(serverId),
    (data) =>
      data && {
        ...data,
        items: data.items.map((m) =>
          m.userId === userId && m.muted !== muted ? { ...m, muted } : m
        )
      }
  )
}

/** Gửi tin bị BE chặn vì mute → khoá ô chat ngay (không chờ reload), rồi refetch member để đồng bộ. */
export function useMutedSendError(serverId: string, onMuted: () => void) {
  const queryClient = useQueryClient()
  const handleMuted = useEffectEvent(onMuted)

  useEffect(
    () =>
      addStompErrorListener((error) => {
        if (error.code !== MUTED_ERROR_CODE) return
        setMyMuted(queryClient, serverId, true)
        queryClient.invalidateQueries({
          queryKey: serverKeys.members(serverId)
        })
        handleMuted()
      }),
    [serverId, queryClient]
  )
}

/** Subscribe topic của channel; tự subscribe lại sau mỗi lần STOMP reconnect. */
export function useChannelRealtime(serverId: string, channelId: string) {
  const queryClient = useQueryClient()
  const connectionId = useStompStatus((s) =>
    s.status === 'connected' ? s.connectionId : 0
  )

  useEffect(() => {
    if (!connectionId) return
    const key = chatKeys.messages(channelId)
    const unsubscribe = subscribeJson<ChatMessage>(
      STOMP.channelTopic(channelId),
      (message) => {
        queryClient.setQueryData<MessagePages>(key, (data) =>
          appendMessage(data, message)
        )
        // Tin của chính mình đi qua được → chắc chắn đã hết mute.
        if (message.senderId === useSession.getState().user?.id)
          setMyMuted(queryClient, serverId, false)
      }
    )
    // Reconnect thì có thể lỡ tin trong lúc mất kết nối → tải lại.
    if (connectionId > 1) queryClient.invalidateQueries({ queryKey: key })
    return unsubscribe
  }, [serverId, channelId, connectionId, queryClient])
}

/** Không có ack: tin hiện ra khi quay về qua topic. Trả false nếu chưa kết nối. */
export const sendChatMessage = (channelId: string, content: string) =>
  publishJson(STOMP.send, { channelId, content })
