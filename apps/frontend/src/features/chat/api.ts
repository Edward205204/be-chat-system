import { request } from '@/lib/http'
import type { ChatMessage, CursorPage, CursorPageQuery } from '@/types/api'

export const chatApi = {
  /** Trang đầu = tin mới nhất; `nextCursor` dẫn tới tin cũ hơn; trong trang đã sắp cũ → mới. */
  messages: (channelId: string, query: CursorPageQuery) =>
    request<CursorPage<ChatMessage>>({
      url: `/chat/${channelId}/messages`,
      params: query
    })
}
