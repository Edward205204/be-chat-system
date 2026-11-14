import { request } from '@/lib/http'
import type {
  CreateRoleRequest,
  GetPermissionResponse,
  RoleMemberResponse,
  RolePatchUpdateRequest,
  RoleResponse,
  ServerPermissionKey
} from '@/types/api'

const roles = (serverId: string) => `/servers/${serverId}/roles`
const role = (serverId: string, roleId: string) =>
  `${roles(serverId)}/${roleId}`

export const roleApi = {
  list: (serverId: string) => request<RoleResponse[]>({ url: roles(serverId) }),
  create: (serverId: string, body: CreateRoleRequest) =>
    request<RoleResponse>({ method: 'POST', url: roles(serverId), data: body }),
  update: (serverId: string, roleId: string, body: RolePatchUpdateRequest) =>
    request<RoleResponse>({
      method: 'PATCH',
      url: role(serverId, roleId),
      data: body
    }),
  remove: (serverId: string, roleId: string) =>
    request<void>({ method: 'DELETE', url: role(serverId, roleId) }),

  members: (serverId: string, roleId: string) =>
    request<RoleMemberResponse[]>({ url: `${role(serverId, roleId)}/members` }),
  addMember: (serverId: string, roleId: string, memberId: string) =>
    request<void>({
      method: 'POST',
      url: `${role(serverId, roleId)}/members`,
      data: { memberId }
    }),
  removeMember: (serverId: string, roleId: string, memberId: string) =>
    request<void>({
      method: 'DELETE',
      url: `${role(serverId, roleId)}/members/${memberId}`
    }),

  /** `permissions` là mảng object `{ permission }` → map về mảng key. */
  permissions: (serverId: string, roleId: string) =>
    request<GetPermissionResponse>({
      url: `${role(serverId, roleId)}/permissions`
    }).then((r) => r.permissions.map((p) => p.permission)),
  grant: (serverId: string, roleId: string, permission: ServerPermissionKey) =>
    request<void>({
      method: 'POST',
      url: `${role(serverId, roleId)}/permissions`,
      data: { permission }
    }),
  revoke: (serverId: string, roleId: string, permission: ServerPermissionKey) =>
    request<void>({
      method: 'DELETE',
      url: `${role(serverId, roleId)}/permissions/${permission}`
    })
}
