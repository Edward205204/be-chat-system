import { useInfiniteQuery, useMutation, useQuery } from '@tanstack/react-query'
import { isFuture } from '@/lib/date'
import { useSession } from '@/lib/session'
import type {
  CreateServerRequest,
  InviteAction,
  ServerPatchUpdateRequest
} from '@/types/api'
import { PAGE_SIZE, serverApi } from './api'

export const serverKeys = {
  all: ['servers'] as const,
  detail: (serverId: string) => ['servers', serverId] as const,
  members: (serverId: string) => ['servers', serverId, 'members'] as const,
  bans: (serverId: string) => ['servers', serverId, 'bans'] as const,
  links: (serverId: string) => ['servers', serverId, 'links'] as const,
  invitations: ['invitations'] as const
}

export function useServersQuery() {
  return useQuery({ queryKey: serverKeys.all, queryFn: serverApi.list })
}

/** Lấy từ danh sách server (đã có `owner`) để khỏi gọi thêm API. */
export function useServer(serverId: string) {
  const query = useServersQuery()
  return { ...query, data: query.data?.find((s) => s.id === serverId) }
}

export function useMembersQuery(serverId: string) {
  return useQuery({
    queryKey: serverKeys.members(serverId),
    queryFn: () => serverApi.members(serverId)
  })
}

/** Bản ghi member của chính mình trong server (để biết `muted`, `memberId`). */
export function useMyMember(serverId: string) {
  const userId = useSession((s) => s.user?.id)
  const { data } = useMembersQuery(serverId)
  return data?.items.find((m) => m.userId === userId)
}

export function useCreateServerMutation() {
  return useMutation({
    mutationFn: (body: CreateServerRequest) => serverApi.create(body),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.all })
  })
}

export function useUpdateServerMutation(serverId: string) {
  return useMutation({
    mutationFn: (body: ServerPatchUpdateRequest) =>
      serverApi.update(serverId, body),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.all })
  })
}

export function useDeleteServerMutation() {
  return useMutation({
    mutationFn: serverApi.remove,
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.all })
  })
}

export function useLeaveServerMutation() {
  return useMutation({
    mutationFn: serverApi.leave,
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.all })
  })
}

type MemberAction =
  | { type: 'kick'; memberId: string }
  | { type: 'mute'; memberId: string; mute: boolean }
  | { type: 'transfer'; memberId: string }
  | { type: 'ban'; userId: string }

export function useMemberActionMutation(serverId: string) {
  return useMutation({
    mutationFn: (action: MemberAction) => {
      switch (action.type) {
        case 'kick':
          return serverApi.kick(serverId, action.memberId)
        case 'mute':
          return serverApi.mute(serverId, action.memberId, action.mute)
        case 'transfer':
          return serverApi.transferOwner(serverId, action.memberId)
        case 'ban':
          return serverApi.ban(serverId, action.userId)
      }
    },
    onSuccess: (_d, action, _r, { client }) => {
      client.invalidateQueries({ queryKey: serverKeys.members(serverId) })
      if (action.type === 'ban')
        client.invalidateQueries({ queryKey: serverKeys.bans(serverId) })
      if (action.type === 'transfer')
        client.invalidateQueries({ queryKey: serverKeys.all })
    }
  })
}

export function useCreateInviteLinkMutation(serverId: string) {
  return useMutation({
    mutationFn: () => serverApi.createInviteLink(serverId),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.links(serverId) })
  })
}

export function useBansQuery(serverId: string) {
  return useInfiniteQuery({
    queryKey: serverKeys.bans(serverId),
    queryFn: ({ pageParam }) => serverApi.bans(serverId, pageParam),
    initialPageParam: 0,
    getNextPageParam: (last, all) =>
      last.length < PAGE_SIZE ? undefined : all.length,
    select: (data) => data.pages.flat()
  })
}

export function useUnbanMutation(serverId: string) {
  return useMutation({
    mutationFn: (userId: string) => serverApi.unban(serverId, userId),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.bans(serverId) })
  })
}

export function useInviteLinksQuery(serverId: string) {
  return useInfiniteQuery({
    queryKey: serverKeys.links(serverId),
    queryFn: ({ pageParam }) => serverApi.inviteLinks(serverId, pageParam),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (last) =>
      last.hasNext ? (last.nextCursor ?? undefined) : undefined,
    select: (data) => data.pages.flatMap((p) => p.data)
  })
}

export function useRevokeInviteLinkMutation(serverId: string) {
  return useMutation({
    mutationFn: (linkId: string) =>
      serverApi.revokeInviteLink(serverId, linkId),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.links(serverId) })
  })
}

export function useJoinServerMutation() {
  return useMutation({
    mutationFn: serverApi.joinByToken,
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: serverKeys.all })
  })
}

export function useInviteUserMutation(serverId: string) {
  return useMutation({
    mutationFn: (inviteeId: string) => serverApi.invite(serverId, inviteeId)
  })
}

/** Chưa có API liệt kê lời mời đã gửi → chỉ huỷ được bằng id vừa tạo. */
export function useCancelInvitationMutation(serverId: string) {
  return useMutation({
    mutationFn: (invitationId: string) =>
      serverApi.cancelInvitation(serverId, invitationId)
  })
}

/** BE trả mọi trạng thái (bug #6) → lọc PENDING còn hạn. Không có realtime nên poll 30s. */
export function usePendingInvitationsQuery() {
  return useQuery({
    queryKey: serverKeys.invitations,
    queryFn: serverApi.invitations,
    select: (list) =>
      list.filter((i) => i.status === 'PENDING' && isFuture(i.expiresAt)),
    refetchInterval: 30_000
  })
}

export function useRespondInvitationMutation() {
  return useMutation({
    mutationFn: ({ id, action }: { id: string; action: InviteAction }) =>
      serverApi.respondInvitation(id, action),
    onSuccess: (_d, _v, _r, { client }) => {
      client.invalidateQueries({ queryKey: serverKeys.invitations })
      client.invalidateQueries({ queryKey: serverKeys.all })
    }
  })
}
