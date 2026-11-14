import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface DismissedState {
  dismissedIds: string[]
  dismiss: (ids: string[]) => void
}

/**
 * BE chưa có API xoá lời mời phía người nhận → "xoá thông báo" chỉ ẩn ở máy này (localStorage).
 * Khi BE có API, thay `dismiss` bằng mutation gọi BE.
 */
export const useDismissedInvitations = create<DismissedState>()(
  persist(
    (set) => ({
      dismissedIds: [],
      dismiss: (ids) =>
        set((s) => ({
          dismissedIds: [...new Set([...s.dismissedIds, ...ids])]
        }))
    }),
    { name: 'rchat-dismissed-invitations' }
  )
)
