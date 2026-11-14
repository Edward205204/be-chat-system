import { useMutation, useQuery } from '@tanstack/react-query'
import { serverKeys } from '@/features/servers/queries'
import type {
  CreateRoleRequest,
  RolePatchUpdateRequest,
  ServerPermissionKey
} from '@/types/api'
import { roleApi } from './api'

export const roleKeys = {
  list: (serverId: string) => ['servers', serverId, 'roles'] as const,
  members: (serverId: string, roleId: string) =>
    ['servers', serverId, 'roles', roleId, 'members'] as const,
  permissions: (serverId: string, roleId: string) =>
    ['servers', serverId, 'roles', roleId, 'permissions'] as const
}

export function useRolesQuery(serverId: string) {
  return useQuery({
    queryKey: roleKeys.list(serverId),
    queryFn: () => roleApi.list(serverId)
  })
}

export function useRoleMembersQuery(serverId: string, roleId: string) {
  return useQuery({
    queryKey: roleKeys.members(serverId, roleId),
    queryFn: () => roleApi.members(serverId, roleId)
  })
}

export function useRolePermissionsQuery(serverId: string, roleId: string) {
  return useQuery({
    queryKey: roleKeys.permissions(serverId, roleId),
    queryFn: () => roleApi.permissions(serverId, roleId)
  })
}

export function useCreateRoleMutation(serverId: string) {
  return useMutation({
    mutationFn: (body: CreateRoleRequest) => roleApi.create(serverId, body),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: roleKeys.list(serverId) })
  })
}

export function useUpdateRoleMutation(serverId: string, roleId: string) {
  return useMutation({
    mutationFn: (body: RolePatchUpdateRequest) =>
      roleApi.update(serverId, roleId, body),
    onSuccess: (_d, _v, _r, { client }) => {
      client.invalidateQueries({ queryKey: roleKeys.list(serverId) })
      client.invalidateQueries({ queryKey: serverKeys.members(serverId) })
    }
  })
}

export function useDeleteRoleMutation(serverId: string) {
  return useMutation({
    mutationFn: (roleId: string) => roleApi.remove(serverId, roleId),
    onSuccess: (_d, _v, _r, { client }) => {
      client.invalidateQueries({ queryKey: roleKeys.list(serverId) })
      client.invalidateQueries({ queryKey: serverKeys.members(serverId) })
    }
  })
}

export function useRoleMemberMutation(serverId: string, roleId: string) {
  return useMutation({
    mutationFn: ({
      memberId,
      assign
    }: {
      memberId: string
      assign: boolean
    }) =>
      assign
        ? roleApi.addMember(serverId, roleId, memberId)
        : roleApi.removeMember(serverId, roleId, memberId),
    onSuccess: (_d, _v, _r, { client }) => {
      client.invalidateQueries({ queryKey: roleKeys.members(serverId, roleId) })
      client.invalidateQueries({ queryKey: serverKeys.members(serverId) })
    }
  })
}

export function useRolePermissionMutation(serverId: string, roleId: string) {
  return useMutation({
    mutationFn: ({
      permission,
      enabled
    }: {
      permission: ServerPermissionKey
      enabled: boolean
    }) =>
      enabled
        ? roleApi.grant(serverId, roleId, permission)
        : roleApi.revoke(serverId, roleId, permission),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({
        queryKey: roleKeys.permissions(serverId, roleId)
      })
  })
}
