import { useQuery } from '@tanstack/react-query'
import { isFuture } from '@/lib/date'
import { serverApi } from '@/features/servers/api'
import { serverKeys } from '@/features/servers/queries'
import type { DirectInvitationResponse } from '@/types/api'
import { useDismissedInvitations } from './store'

export interface InvitationNotification extends DirectInvitationResponse {
  /** Còn nhận/từ chối được: PENDING và chưa hết hạn. */
  isActionable: boolean
}

/** Mới nhất lên đầu, đã lọc thông báo người dùng xoá. Dùng chung cache với lời mời ở servers. */
export function useInvitationNotifications() {
  const dismissedIds = useDismissedInvitations((s) => s.dismissedIds)
  return useQuery({
    queryKey: serverKeys.invitations,
    queryFn: serverApi.invitations,
    refetchInterval: 30_000,
    select: (list): InvitationNotification[] =>
      list
        .filter((i) => !dismissedIds.includes(i.id))
        .map((i) => ({
          ...i,
          isActionable: i.status === 'PENDING' && isFuture(i.expiresAt)
        }))
        .toSorted((a, b) => b.createdAt.localeCompare(a.createdAt))
  })
}

/** Số lời mời còn nhận được (cho badge). */
export function usePendingInvitationCount() {
  const { data = [] } = useInvitationNotifications()
  return data.filter((n) => n.isActionable).length
}
