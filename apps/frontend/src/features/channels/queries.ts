import { useMutation, useQuery } from '@tanstack/react-query'
import type { ChannelPermissionKey, CreateChannelRequest } from '@/types/api'
import { channelApi } from './api'

export const channelKeys = {
  list: (serverId: string) => ['servers', serverId, 'channels'] as const,
  permissions: (serverId: string, channelId: string) =>
    ['servers', serverId, 'channels', channelId, 'permissions'] as const
}

export function useChannelsQuery(serverId: string) {
  return useQuery({
    queryKey: channelKeys.list(serverId),
    queryFn: () => channelApi.list(serverId)
  })
}

export function useCreateChannelMutation(serverId: string) {
  return useMutation({
    mutationFn: (body: CreateChannelRequest) =>
      channelApi.create(serverId, body),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: channelKeys.list(serverId) })
  })
}

export function useRenameChannelMutation(serverId: string) {
  return useMutation({
    mutationFn: ({ channelId, name }: { channelId: string; name: string }) =>
      channelApi.rename(serverId, channelId, name),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: channelKeys.list(serverId) })
  })
}

export function useDeleteChannelMutation(serverId: string) {
  return useMutation({
    mutationFn: (channelId: string) => channelApi.remove(serverId, channelId),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({ queryKey: channelKeys.list(serverId) })
  })
}

export function useChannelPermissionsQuery(
  serverId: string,
  channelId: string
) {
  return useQuery({
    queryKey: channelKeys.permissions(serverId, channelId),
    queryFn: () => channelApi.permissions(serverId, channelId)
  })
}

type ChannelPermissionChange = {
  target: 'role' | 'member'
  id: string
  permission: ChannelPermissionKey
  enabled: boolean
}

export function useChannelPermissionMutation(
  serverId: string,
  channelId: string
) {
  return useMutation({
    mutationFn: ({
      target,
      id,
      permission,
      enabled
    }: ChannelPermissionChange) => {
      if (target === 'role')
        return enabled
          ? channelApi.grantRole(serverId, channelId, id, permission)
          : channelApi.revokeRole(serverId, channelId, id, permission)
      return enabled
        ? channelApi.grantMember(serverId, channelId, id, permission)
        : channelApi.revokeMember(serverId, channelId, id, permission)
    },
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({
        queryKey: channelKeys.permissions(serverId, channelId)
      })
  })
}

export function useInviteToChannelMutation(
  serverId: string,
  channelId: string
) {
  return useMutation({
    mutationFn: (memberId: string) =>
      channelApi.inviteMember(serverId, channelId, memberId),
    onSuccess: (_d, _v, _r, { client }) =>
      client.invalidateQueries({
        queryKey: channelKeys.permissions(serverId, channelId)
      })
  })
}
