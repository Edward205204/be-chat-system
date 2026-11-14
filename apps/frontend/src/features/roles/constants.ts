import type { ChannelPermissionKey, ServerPermissionKey } from '@/types/api'

export const EVERYONE_ROLE = '@everyone'

// MANAGE_ROLES chỉ owner dùng được (BE chặn cứng) → không cho gán.
export const SERVER_PERMISSIONS: readonly {
  key: ServerPermissionKey
  label: string
}[] = [
  { key: 'CREATE_INVITE', label: 'Tạo lời mời' },
  { key: 'MANAGE_CHANNELS', label: 'Tạo channel' },
  { key: 'MANAGE_SERVER', label: 'Quản lý server' },
  { key: 'KICK_MEMBER', label: 'Kick' },
  { key: 'BAN_MEMBER', label: 'Ban' },
  { key: 'MUTE_MEMBER', label: 'Mute' }
]

// SEND_MESSAGES, MANAGE_MESSAGES BE chưa dùng → ẩn.
export const CHANNEL_PERMISSIONS: readonly {
  key: ChannelPermissionKey
  label: string
}[] = [
  { key: 'VIEW_CHANNEL', label: 'Xem & chat' },
  { key: 'MANAGE_CHANNEL', label: 'Sửa/xoá channel' },
  { key: 'MANAGE_CHANNEL_PERMISSIONS', label: 'Quản lý quyền' },
  { key: 'INVITE_MEMBERS', label: 'Thêm member' }
]
