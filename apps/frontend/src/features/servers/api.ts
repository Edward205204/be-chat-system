import { request } from '@/lib/http'
import type {
  CreateDirectInviteResponse,
  CursorPage,
  CreateServerRequest,
  DirectInvitationResponse,
  InviteAction,
  InviteLinkResponse,
  ServerMemberResponse,
  ServerPatchUpdateRequest,
  ServerBanResponse,
  ServerPatchUpdateResponse,
  ServerResponse,
  UserJoinServerByLinkResponse
} from '@/types/api'

const s = (serverId: string) => `/servers/${serverId}`

/** Size mỗi trang khi danh sách gọi API phân trang ("Tải thêm"). */
export const PAGE_SIZE = 20

export const serverApi = {
  list: () => request<ServerResponse[]>({ url: '/servers' }),
  get: (serverId: string) => request<ServerResponse>({ url: s(serverId) }),
  create: (body: CreateServerRequest) =>
    request<ServerResponse>({ method: 'POST', url: '/servers', data: body }),
  update: (serverId: string, body: ServerPatchUpdateRequest) =>
    request<ServerPatchUpdateResponse>({
      method: 'PATCH',
      url: s(serverId),
      data: body
    }),
  remove: (serverId: string) =>
    request<void>({ method: 'DELETE', url: s(serverId) }),
  leave: (serverId: string) =>
    request<void>({ method: 'DELETE', url: `${s(serverId)}/members/me` }),

  members: (serverId: string) =>
    request<ServerMemberResponse>({
      url: `${s(serverId)}/members`,
      params: { page: 0, size: 100 }
    }),
  kick: (serverId: string, memberId: string) =>
    request<void>({
      method: 'DELETE',
      url: `${s(serverId)}/members/${memberId}`
    }),
  mute: (serverId: string, memberId: string, mute: boolean) =>
    request<void>({
      method: 'PATCH',
      url: `${s(serverId)}/members/${memberId}/mute`,
      data: { mute }
    }),
  transferOwner: (serverId: string, memberId: string) =>
    request<void>({ method: 'PATCH', url: `${s(serverId)}/owner/${memberId}` }),
  ban: (serverId: string, userId: string, reason?: string) =>
    request<void>({
      method: 'POST',
      url: `${s(serverId)}/bans/${userId}`,
      data: { reason }
    }),

  /** Trả mảng trần, không có total: trang ngắn hơn `PAGE_SIZE` là trang cuối. */
  bans: (serverId: string, page: number) =>
    request<ServerBanResponse[]>({
      url: `${s(serverId)}/bans`,
      params: { page, size: PAGE_SIZE }
    }),
  unban: (serverId: string, userId: string) =>
    request<void>({ method: 'DELETE', url: `${s(serverId)}/bans/${userId}` }),

  /** Sắp mới → cũ, gồm cả link đã hết hạn. */
  inviteLinks: (serverId: string, cursor?: string) =>
    request<CursorPage<InviteLinkResponse>>({
      url: `${s(serverId)}/links`,
      params: { cursor, size: PAGE_SIZE }
    }),
  /** BE xoá hẳn link (bug #4), `revoked` luôn false. */
  revokeInviteLink: (serverId: string, linkId: string) =>
    request<void>({
      method: 'PATCH',
      url: `${s(serverId)}/links/${linkId}/revoke`
    }),
  createInviteLink: (serverId: string) =>
    request<InviteLinkResponse>({
      method: 'POST',
      url: `${s(serverId)}/links`
    }),
  joinByToken: (token: string) =>
    request<UserJoinServerByLinkResponse>({
      method: 'POST',
      url: `/servers/join/${encodeURIComponent(token)}`
    }),

  invite: (serverId: string, inviteeId: string) =>
    request<CreateDirectInviteResponse>({
      method: 'POST',
      url: `${s(serverId)}/invitations`,
      data: { inviteeId }
    }),
  cancelInvitation: (serverId: string, invitationId: string) =>
    request<void>({
      method: 'DELETE',
      url: `${s(serverId)}/invitations/${invitationId}`
    }),
  invitations: () =>
    request<DirectInvitationResponse[]>({ url: '/servers/invitations' }),
  respondInvitation: (invitationId: string, action: InviteAction) =>
    request<void>({
      method: 'PATCH',
      url: `/servers/invitations/${invitationId}`,
      data: { action }
    })
}

/** `inviteUrl` của BE sai (bug #3) → tự ghép từ token. */
export const buildInviteUrl = (token: string) =>
  `${window.location.origin}/invite/${token}`

/** Nhận link đầy đủ hoặc token trần. */
export const extractInviteToken = (input: string) =>
  input.trim().split('/').filter(Boolean).pop() ?? ''
