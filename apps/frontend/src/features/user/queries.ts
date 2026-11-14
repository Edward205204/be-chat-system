import { useMutation, useQuery } from '@tanstack/react-query'
import { useSession } from '@/lib/session'
import type {
  UserPatchUpdateRequest,
  UserPublicResponse,
  UserResponse
} from '@/types/api'
import { PUBLIC_PROFILE_ENABLED } from './constants'
import { userApi } from './api'

export const userKeys = {
  me: ['users', 'me'] as const,
  detail: (userId: string) => ['users', userId] as const
}

export function useMeQuery() {
  return useQuery({
    queryKey: userKeys.me,
    queryFn: async () => {
      const me = await userApi.getMe()
      useSession.getState().setUser(me)
      return me
    },
    initialData: useSession.getState().user ?? undefined,
    staleTime: 60_000
  })
}

export function useUpdateMeMutation() {
  return useMutation({
    mutationFn: (body: UserPatchUpdateRequest) => userApi.updateMe(body),
    onSuccess: (me, _vars, _ctx, { client }) => {
      useSession.getState().setUser(me)
      client.setQueryData(userKeys.me, me)
    }
  })
}

export function useSearchUserMutation() {
  return useMutation({ mutationFn: (q: string) => userApi.search(q) })
}

export type ProfileUser = UserResponse | UserPublicResponse

/**
 * Hồ sơ theo userId. Của mình → dùng /users/me (có email, ngày sinh).
 * Người khác → /users/{id} khi `PUBLIC_PROFILE_ENABLED`, chưa bật thì `isSupported = false`.
 */
export function useUser(userId: string) {
  const myId = useSession((s) => s.user?.id)
  const isOwner = userId === myId
  const isSupported = isOwner || PUBLIC_PROFILE_ENABLED
  const me = useMeQuery()
  const other = useQuery({
    queryKey: userKeys.detail(userId),
    queryFn: () => userApi.getById(userId),
    enabled: !isOwner && PUBLIC_PROFILE_ENABLED
  })
  const query = isOwner ? me : other
  return {
    user: query.data as ProfileUser | undefined,
    isPending: isSupported && query.isPending,
    isOwner,
    isSupported
  }
}
