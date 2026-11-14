// chat_system API types — dựng từ DTO backend ngày 2026-11-07.
// Xem README.md cho quy ước. Base URL: /rchat. Mọi id là UUID string.
// LocalDateTime = chuỗi ISO không timezone, vd "2026-11-07T18:30:12.345678".

export type ISODateTime = string
export type ISODate = string // "YYYY-MM-DD"

// ───────────────────────── Envelope ─────────────────────────
export interface ApiResponse<T> {
  status: 'SUCCESS' | ErrorStatus
  message?: string
  result?: T // bị bỏ khỏi JSON khi null
}

export interface CursorPage<T> {
  data: T[]
  nextCursor: string | null
  hasNext: boolean
}

export interface CursorPageQuery {
  cursor?: string
  size?: number // 1..100, mặc định 20
}

export interface OffsetPageQuery {
  page?: number // 0-based
  size?: number // mặc định 50, tối đa 100
}

// ───────────────────────── Enums ─────────────────────────
export type ServerPermissionKey =
  | 'MANAGE_CHANNELS'
  | 'CREATE_INVITE'
  | 'MANAGE_SERVER'
  | 'KICK_MEMBER'
  | 'BAN_MEMBER'
  | 'MUTE_MEMBER'
  | 'MANAGE_ROLES'

export type ChannelPermissionKey =
  | 'VIEW_CHANNEL'
  | 'MANAGE_CHANNEL'
  | 'MANAGE_CHANNEL_PERMISSIONS'
  | 'INVITE_MEMBERS'
  | 'SEND_MESSAGES'
  | 'MANAGE_MESSAGES'

export type InviteStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'
export type InviteAction = 'ACCEPT' | 'REJECT'
export type TokenType = 'ACCESS_TOKEN' | 'REFRESH_TOKEN' | 'TMP_TOKEN'

export type ErrorStatus =
  | 'INVALID_JSON'
  | 'INVALID_CURSOR'
  | 'UNAUTHENTICATED'
  | 'LOGIN_FAILED'
  | 'UNAUTHORIZED'
  | 'NOT_A_MEMBER'
  | 'MISSING_PERMISSION'
  | 'NOW_DO_NOT_HAVE_PERMISSION'
  | 'USER_BANNED'
  | 'EMAIL_NOT_FOUND'
  | 'USER_NOT_FOUND'
  | 'SERVER_NOT_EXIST'
  | 'ROLE_NOT_EXIST'
  | 'INVITE_LINK_NOT_FOUND'
  | 'FILE_NOT_FOUND'
  | 'DIRECT_INVITE_NOT_FOUND'
  | 'CHANNEL_IS_NOT_EXIST'
  | 'DIRECT_INVITE_HAS_USED'
  | 'CANNOT_CANCEL_INVITE'
  | 'OTP_DOES_NOT_EXIST'
  | 'OTP_INCORRECT'
  | 'OTP_EXPIRED'
  | 'OTP_HAS_BEEN_USED'
  | 'EMAIL_EXISTED'
  | 'USERNAME_EXISTED'
  | 'USER_VERIFIED'
  | 'USER_ALREADY_A_MEMBER'
  | 'SERVER_NAME_DUPLICATE'
  | 'CHANNEL_NAME_DUPLICATE'
  | 'ROLE_NAME_DUPLICATE ' // có dấu cách cuối, đúng như BE
  | 'USE_ALREADY_ASSIGNED_FOR_THIS_ROLE' // thiếu chữ R, đúng như BE
  | 'PERMISSION_DUPLICATE_FOR_THIS_ROLE'
  | 'PERMISSION_DUPLICATE_FOR_THIS_USER'
  | 'DIRECT_INVITE_ALREADY_PENDING'
  | 'OTP_COOLDOWN'
  | 'OTP_REVOKED'
  | 'OTP_MAX_ATTEMPTS_EXCEEDED'
  | 'JWT_SIGNING_FAILED'
  | 'FILE_STORAGE_ERROR'
  | 'UNCATEGORIZED'

// ───────────────────────── JWT payload ─────────────────────────
export interface JwtPayload {
  sub: string // userId
  email: string
  username: string
  tokenType: TokenType
  iss: 'edward.com'
  iat: number
  exp: number
  jti: string
}

// ───────────────────────── Auth ─────────────────────────
export interface RegisterRequest {
  email: string
  password: string // 8-20, hoa+thường+số+ký tự đặc biệt !@#$%^&*()-+=_
  username: string // ^[a-z0-9_]{3,20}$
  displayName: string // 1-20
  dateOfBirth: ISODate // quá khứ
}
export interface LoginRequest {
  email: string
  password: string
}
export interface VerifyEmailRequest {
  otp: string
} // 6 ký tự
export interface TokenRequest {
  refreshToken: string
}
export interface ForgotPasswordRequest {
  email: string
}
export interface ResetPasswordRequest {
  email: string
  otp: string
  password: string
}

export interface UnverifiedResponse {
  tmpToken: string
}
export interface AuthSuccessResponse {
  accessToken: string // chuỗi phẳng
  refreshToken: string
  user: UserResponse
}
export type LoginResponse = AuthSuccessResponse | UnverifiedResponse
export const isUnverified = (r: LoginResponse): r is UnverifiedResponse =>
  'tmpToken' in r

export interface SignedToken {
  token: string
  expiresAt: string
}
export interface TokenResponse {
  // verify-email & refresh: dạng LỒNG
  accessToken: SignedToken
  refreshToken: SignedToken
}

// ───────────────────────── User ─────────────────────────
export interface UserResponse {
  id: string
  username: string
  email: string
  displayName: string
  avatar: string | null
  banner: string | null
  dateOfBirth: ISODate | null
  createdAt: ISODateTime
}
export interface UserPublicResponse {
  id: string
  username: string
  displayName: string
  avatar: string | null
  banner: string | null
  createdAt: ISODateTime
}
export interface UserBasicInfo {
  id: string
  username: string
  displayName: string
  avatar: string | null
}
/** Ít nhất 1 field. avatar/banner phải là URL tuyệt đối. */
export interface UserPatchUpdateRequest {
  displayName?: string
  username?: string
  avatar?: string
  banner?: string
  dateOfBirth?: ISODate
}

// ───────────────────────── Server ─────────────────────────
export interface ServerResponse {
  id: string
  name: string
  avatar: string | null
  banner: string | null
  ownerId: string
  owner: boolean // Java isOwner
  joinedAt: ISODateTime
}
export interface CreateServerRequest {
  name: string
  avatar?: string
}
/** name bắt buộc dù là PATCH. */
export interface ServerPatchUpdateRequest {
  name: string
  avatar?: string
  banner?: string
}
export interface ServerPatchUpdateResponse {
  id: string
  name: string
  avatar: string | null
  banner: string | null
}
export interface ServerBasicInfo {
  id: string
  name: string
  avatar: string | null
}

export interface ServerMemberItem {
  memberId: string
  userId: string
  displayName: string
  username: string
  avatar: string | null
  owner: boolean // Java isOwner
  muted: boolean // Java isMuted
  joinedAt: ISODateTime
  roles: RoleResponse[] // createdAt null ở đây; không gồm @everyone
}
export interface ServerMemberResponse {
  total: number
  page: number
  size: number
  items: ServerMemberItem[]
}
export interface MuteMemberRequest {
  mute: boolean
}
export interface BanMemberRequest {
  reason?: string
}
export interface ServerBanResponse {
  id: string
  user: UserBasicInfo
  bannedBy: UserBasicInfo
  reason: string | null
  createdAt: ISODateTime
}

export interface InviteLinkResponse {
  id: string
  token: string
  inviteUrl: string // KHÔNG dùng (bug), tự ghép từ token
  createdBy: UserBasicInfo
  useCount: number
  expiresAt: ISODateTime
  createdAt: ISODateTime
  revoked: boolean // Java isRevoked, luôn false
}
export interface UserJoinServerByLinkResponse {
  serverId: string
  serverName: string
  joinedAt: ISODateTime
}

export interface DirectInviteRequest {
  inviteeId: string
} // userId
export interface ResponseDirectInviteRequest {
  action: InviteAction
}
export interface CreateDirectInviteResponse {
  id: string
  server: ServerBasicInfo
  inviter: UserBasicInfo
  invitee: UserBasicInfo
  status: InviteStatus
  expiresAt: ISODateTime
  createdAt: ISODateTime
}
export interface DirectInvitationResponse {
  id: string
  server: ServerBasicInfo // avatar luôn null (bug)
  inviter: UserBasicInfo
  status: InviteStatus
  expiresAt: ISODateTime
  createdAt: ISODateTime
}

// ───────────────────────── Channel ─────────────────────────
export interface ChannelResponse {
  id: string
  name: string
  private: boolean // Java isPrivate
  createdAt: ISODateTime
}
/** Request dùng `isPrivate` (bắt buộc), khác response trả `private`. */
export interface CreateChannelRequest {
  name: string
  isPrivate: boolean
}
export interface ChannelPatchUpdateRequest {
  name: string
}
export interface AddToChannelRequest {
  memberId: string
}

// ───────────────────────── Role & permission ─────────────────────────
export interface RoleResponse {
  id: string
  name: string
  color: string // "#RRGGBB"
  createdAt: ISODateTime | null
}
export interface CreateRoleRequest {
  name: string
  color: string
} // name 3-20, color ^#[0-9A-Fa-f]{6}$
export interface RolePatchUpdateRequest {
  name?: string
  color?: string
}
export interface AddRoleMemberRequest {
  memberId: string
}
export interface RoleMemberResponse {
  memberId: string
  userId: string
  displayName: string
  username: string
  avatar: string | null
}

export interface GetPermissionResponse {
  roleId: string
  permissions: { permission: ServerPermissionKey }[] // mảng object
}
export interface AddPermissionRequest {
  permission: ServerPermissionKey
}
export interface ServerPermissionPutUpdateRequest {
  permission: ServerPermissionKey[]
} // không rỗng
export interface RoleWithPermissionResponse {
  roleId: string
  permissions: ServerPermissionKey[] // mảng chuỗi
}

export interface ChannelPermissionConfigDataResponse {
  channelId: string
  private: boolean // Java isPrivate
  rolePermissions: {
    roleId: string
    roleName: string
    permissions: ChannelPermissionKey[]
  }[]
  userPermissions: {
    memberId: string
    userId: string
    displayName: string
    permissions: ChannelPermissionKey[]
  }[]
}
export interface AddChannelPermissionForRoleRequest {
  roleId: string
  permission: ChannelPermissionKey
}
export interface AddChannelPermissionForUserRequest {
  memberId: string
  permission: ChannelPermissionKey
}
export interface ChannelPermissionPutUpdateRequest {
  permission: ChannelPermissionKey[]
} // không rỗng

// ───────────────────────── Chat ─────────────────────────
export interface ChatMessage {
  id: string
  channelId: string
  senderId: string
  senderName: string // username
  senderAvatar: string | null
  content: string
  createdAt: ISODateTime
}
export interface SendMessageRequest {
  channelId: string
  content: string
}
export interface StompErrorPayload {
  status: ErrorStatus
  message: string
} // /user/queue/errors

export const STOMP = {
  endpoint: '/rchat/ws',
  send: '/app/chat.send',
  channelTopic: (channelId: string) => `/topic/channel/${channelId}`,
  errors: '/user/queue/errors'
} as const
