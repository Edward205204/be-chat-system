import { request } from '@/lib/http'
import type {
  ChannelPermissionConfigDataResponse,
  ChannelPermissionKey,
  ChannelResponse,
  CreateChannelRequest,
  CursorPage
} from '@/types/api'

const base = (serverId: string) => `/servers/${serverId}/channels`

export const channelApi = {
  /** Server ít channel: lấy 100 một lần là đủ (04-channels 2.1). */
  list: (serverId: string) =>
    request<CursorPage<ChannelResponse>>({
      url: base(serverId),
      params: { size: 100 }
    }).then((p) => p.data),
  create: (serverId: string, body: CreateChannelRequest) =>
    request<ChannelResponse>({
      method: 'POST',
      url: base(serverId),
      data: body
    }),
  rename: (serverId: string, channelId: string, name: string) =>
    request<ChannelResponse>({
      method: 'PATCH',
      url: `${base(serverId)}/${channelId}`,
      data: { name }
    }),
  remove: (serverId: string, channelId: string) =>
    request<void>({ method: 'DELETE', url: `${base(serverId)}/${channelId}` }),

  /** Thêm member vào channel private = cấp VIEW_CHANNEL cho memberId. */
  inviteMember: (serverId: string, channelId: string, memberId: string) =>
    request<void>({
      method: 'POST',
      url: `${base(serverId)}/${channelId}/invite`,
      data: { memberId }
    }),

  permissions: (serverId: string, channelId: string) =>
    request<ChannelPermissionConfigDataResponse>({
      url: `${base(serverId)}/${channelId}/permissions`
    }),
  grantRole: (
    serverId: string,
    channelId: string,
    roleId: string,
    permission: ChannelPermissionKey
  ) =>
    request<void>({
      method: 'POST',
      url: `${base(serverId)}/${channelId}/permissions/roles`,
      data: { roleId, permission }
    }),
  revokeRole: (
    serverId: string,
    channelId: string,
    roleId: string,
    permission: ChannelPermissionKey
  ) =>
    request<void>({
      method: 'DELETE',
      url: `${base(serverId)}/${channelId}/permissions/roles/${roleId}/permission/${permission}`
    }),
  grantMember: (
    serverId: string,
    channelId: string,
    memberId: string,
    permission: ChannelPermissionKey
  ) =>
    request<void>({
      method: 'POST',
      url: `${base(serverId)}/${channelId}/permissions/users`,
      data: { memberId, permission }
    }),
  revokeMember: (
    serverId: string,
    channelId: string,
    memberId: string,
    permission: ChannelPermissionKey
  ) =>
    request<void>({
      method: 'DELETE',
      url: `${base(serverId)}/${channelId}/permissions/users/${memberId}/permission/${permission}`
    })
}
