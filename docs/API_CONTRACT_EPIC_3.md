# API Contract — Epic 3: Server

## Quy ước chung

- Base URL: `/servers`
- Authentication: tất cả endpoint đều yêu cầu `Authorization: Bearer <access_token>`, trừ khi ghi rõ "public".
- Response envelope thống nhất:
  ```
  {
    "code": "SUCCESS" | "ERROR_CODE",
    "message": "...",
    "result": { ... } | null
  }
  ```
- Các trường timestamp trả về dạng ISO 8601: `"2025-06-01T10:30:00Z"`
- `currentUserId` = user lấy từ JWT token, không truyền trong body.

---

## US31 — Tạo server

### POST /servers

Tạo server mới. Người tạo tự động trở thành Owner và được thêm vào `server_members`. Hệ thống tự tạo kèm:
- Role `@everyone` (is_everyone = true) với không có permission nào được grant mặc định.
- Channel mặc định tên `general` (public — `VIEW_CHANNEL` của `@everyone` được ALLOW).

**Request body** (multipart/form-data):

- `name` (string, required) — tên server, tối đa 100 ký tự.
- `avatar` (file, optional) — ảnh avatar server.

**Response 201:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "srv_abc123",
    "name": "My Server",
    "avatar": "https://cdn.example.com/servers/srv_abc123/avatar.png",
    "banner": null,
    "ownerId": "usr_xyz",
    "createdAt": "2025-06-01T10:00:00Z",
    "defaultChannel": {
      "id": "ch_001",
      "name": "general"
    }
  }
}
```

**Lỗi:**
- `400 VALIDATION_ERROR` — `name` trống hoặc vượt 100 ký tự.

---

## US32 — Cài đặt server

### GET /servers/:serverId

Lấy thông tin chi tiết server. Chỉ thành viên của server mới xem được.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "srv_abc123",
    "name": "My Server",
    "avatar": "https://cdn.example.com/...",
    "banner": null,
    "ownerId": "usr_xyz",
    "createdAt": "2025-06-01T10:00:00Z"
  }
}
```

**Lỗi:**
- `403 FORBIDDEN` — không phải thành viên server.
- `404 SERVER_NOT_FOUND`

---

### PATCH /servers/:serverId

Cập nhật thông tin server. Yêu cầu quyền `MANAGE_SERVER` hoặc là Owner.

**Request body** (multipart/form-data, tất cả optional):

- `name` (string) — tên mới.
- `avatar` (file) — avatar mới.
- `banner` (file) — banner mới.

> Chỉ truyền trường nào muốn update. Không truyền = giữ nguyên.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "srv_abc123",
    "name": "Updated Name",
    "avatar": "https://cdn.example.com/...",
    "banner": "https://cdn.example.com/...",
    "ownerId": "usr_xyz",
    "createdAt": "2025-06-01T10:00:00Z"
  }
}
```

**Lỗi:**
- `403 FORBIDDEN` — không có quyền `MANAGE_SERVER`.
- `404 SERVER_NOT_FOUND`

---

### GET /servers/:serverId/members

Lấy danh sách thành viên server. Chỉ thành viên server mới xem được.

**Query params:**
- `page` (int, default 1)
- `size` (int, default 20, max 100)

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "total": 42,
    "page": 1,
    "size": 20,
    "items": [
      {
        "serverId": "srv_abc123",
        "userId": "usr_xyz",
        "displayName": "Edward",
        "username": "edward_dev",
        "avatar": "https://cdn.example.com/...",
        "isOwner": true,
        "roles": [
          { "id": "role_001", "name": "Admin", "color": "#FF5733" }
        ],
        "joinedAt": "2025-06-01T10:00:00Z"
      }
    ]
  }
}
```

---

### DELETE /servers/:serverId/members/:userId

Kick thành viên khỏi server. Yêu cầu quyền `KICK_MEMBER`. Không thể kick Owner.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `403 FORBIDDEN` — không có quyền `KICK_MEMBER`, hoặc target là Owner.
- `404 MEMBER_NOT_FOUND`

---

### POST /servers/:serverId/bans/:userId

Ban thành viên khỏi server vĩnh viễn. Yêu cầu quyền `BAN_MEMBER`. Không thể ban Owner.

> **DB note:** V1 chưa có bảng `bans` trong ERD. Cần bổ sung bảng `server_bans (id, server_id, user_id, banned_by, reason, created_at)` để enforce khi user cố join lại bằng invite link.

**Request body:**
```
{
  "reason": "Spam" // optional
}
```

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `403 FORBIDDEN` — không có quyền `BAN_MEMBER`, hoặc target là Owner.
- `404 MEMBER_NOT_FOUND`

---

### POST /servers/:serverId/members/:userId/mute

Mute thành viên trong toàn server (tắt quyền gửi tin nhắn). Yêu cầu quyền `MUTE_MEMBER`.

> **Cách implement:** Set `SEND_MESSAGES = DENY` ở user-level permission (`channel_user_permissions`) cho tất cả channel trong server — hoặc đơn giản hơn, thêm flag `is_muted` vào `server_members`. Cân nhắc thêm `is_muted BOOLEAN` vào `server_members` cho V1 vì permission model sẽ phức tạp hơn.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

---

### DELETE /servers/:serverId/members/:userId/mute

Unmute thành viên.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

---

### PATCH /servers/:serverId/owner

Chuyển quyền Owner sang thành viên khác. Chỉ Owner hiện tại mới được gọi. Bắt buộc khi Owner muốn thoát server (US38).

**Request body:**
```
{
  "newOwnerId": "usr_new_owner"
}
```

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `403 FORBIDDEN` — không phải Owner.
- `404 MEMBER_NOT_FOUND` — `newOwnerId` không phải thành viên server.

---

## US33 — Mời bạn bè trực tiếp vào server

Mời bạn bè hoặc người trong DM gần đây vào server, không qua link. Người được mời nhận notification và tự quyết định accept/decline.

### POST /servers/:serverId/invitations

Gửi lời mời trực tiếp. Yêu cầu quyền `CREATE_INVITE`.

> **DB note:** Cần thêm bảng `server_invitations (id, server_id, inviter_id, invitee_id, status ENUM(PENDING, ACCEPTED, DECLINED), created_at, expires_at)`. V1 có thể set `expires_at = created_at + 7 days`.

**Request body:**
```
{
  "inviteeId": "usr_friend_123"
}
```

**Response 201:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "sinv_001",
    "serverId": "srv_abc123",
    "inviterId": "usr_xyz",
    "inviteeId": "usr_friend_123",
    "status": "PENDING",
    "createdAt": "2025-06-01T10:00:00Z",
    "expiresAt": "2025-06-08T10:00:00Z"
  }
}
```

**Lỗi:**
- `403 FORBIDDEN` — không có quyền `CREATE_INVITE`.
- `409 ALREADY_MEMBER` — invitee đã là thành viên server.
- `409 INVITATION_ALREADY_SENT` — đã có lời mời PENDING với invitee này.
- `403 USER_BANNED` — invitee đang bị ban khỏi server.

---

### GET /me/server-invitations

Lấy danh sách lời mời server đang chờ của current user.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": [
    {
      "id": "sinv_001",
      "server": {
        "id": "srv_abc123",
        "name": "My Server",
        "avatar": "https://cdn.example.com/..."
      },
      "inviter": {
        "userId": "usr_xyz",
        "displayName": "Edward",
        "avatar": "https://cdn.example.com/..."
      },
      "createdAt": "2025-06-01T10:00:00Z",
      "expiresAt": "2025-06-08T10:00:00Z"
    }
  ]
}
```

---

### POST /me/server-invitations/:invitationId/accept

Chấp nhận lời mời → user được thêm vào `server_members`.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "serverId": "srv_abc123",
    "serverName": "My Server"
  }
}
```

**Lỗi:**
- `404 INVITATION_NOT_FOUND`
- `410 INVITATION_EXPIRED`
- `409 ALREADY_MEMBER`

---

### POST /me/server-invitations/:invitationId/decline

Từ chối lời mời.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

---

## US34 — Chia sẻ link mời server

### POST /servers/:serverId/invite-links

Tạo invite link mới. Yêu cầu quyền `CREATE_INVITE`.

`token` được generate ngẫu nhiên (UUID hoặc nanoid). `expires_at = now + 3 days`.

**Response 201:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "il_001",
    "serverId": "srv_abc123",
    "token": "aB3kR9xZ",
    "inviteUrl": "https://chat.example.com/invite/aB3kR9xZ",
    "createdBy": "usr_xyz",
    "useCount": 0,
    "isRevoked": false,
    "expiresAt": "2025-06-04T10:00:00Z",
    "createdAt": "2025-06-01T10:00:00Z"
  }
}
```

**Lỗi:**
- `403 FORBIDDEN` — không có quyền `CREATE_INVITE`.

---

### POST /invite-links/:token/join

Join server bằng invite link. Public endpoint — chỉ cần authenticated (không cần là thành viên server).

Hệ thống kiểm tra: link tồn tại → chưa revoked → chưa hết hạn → user chưa bị ban → add vào `server_members` → tăng `use_count`.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "serverId": "srv_abc123",
    "serverName": "My Server",
    "serverAvatar": "https://cdn.example.com/..."
  }
}
```

**Lỗi:**
- `404 INVITE_LINK_NOT_FOUND`
- `410 INVITE_LINK_EXPIRED` — hết hạn hoặc đã revoked.
- `403 USER_BANNED` — user đang bị ban khỏi server này.
- `409 ALREADY_MEMBER` — đã là thành viên.

---

### GET /invite-links/:token/preview

Preview thông tin server trước khi join. Public endpoint (authenticated).

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "server": {
      "id": "srv_abc123",
      "name": "My Server",
      "avatar": "https://cdn.example.com/...",
      "memberCount": 42
    },
    "inviter": {
      "displayName": "Edward",
      "avatar": "https://cdn.example.com/..."
    },
    "expiresAt": "2025-06-04T10:00:00Z",
    "isValid": true
  }
}
```

---

## US35 — Quản lý danh sách link mời

### GET /servers/:serverId/invite-links

Lấy toàn bộ invite links của server. Yêu cầu quyền `MANAGE_SERVER` hoặc là Owner.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": [
    {
      "id": "il_001",
      "token": "aB3kR9xZ",
      "inviteUrl": "https://chat.example.com/invite/aB3kR9xZ",
      "createdBy": {
        "userId": "usr_xyz",
        "displayName": "Edward",
        "avatar": "https://cdn.example.com/..."
      },
      "useCount": 17,
      "isRevoked": false,
      "expiresAt": "2025-06-04T10:00:00Z",
      "createdAt": "2025-06-01T10:00:00Z",
      "status": "ACTIVE"  // "ACTIVE" | "EXPIRED" | "REVOKED"
    }
  ]
}
```

> `status` là computed field, derive từ `is_revoked` và so sánh `expires_at` với `now`.

---

### DELETE /servers/:serverId/invite-links/:linkId

Revoke (vô hiệu hóa) một invite link. Yêu cầu quyền `MANAGE_SERVER` hoặc là Owner.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `404 INVITE_LINK_NOT_FOUND`
- `409 ALREADY_REVOKED` — link đã bị revoke trước đó.

---

## US36 — Tạo channel

### POST /servers/:serverId/channels

Tạo channel mới. Yêu cầu quyền `MANAGE_CHANNEL` (server-level — tức là `MANAGE_CHANNEL` không phải channel permission, cần xem lại US41 — xem feedback bên dưới).

> **Feedback:** US41 liệt kê server permissions không có `MANAGE_CHANNEL` — đó là channel-level permission (US42). Việc **tạo channel** nên guard bằng một server-level permission riêng. Đề xuất thêm `MANAGE_CHANNELS` (số nhiều) vào `server_permission_key` để phân biệt với `MANAGE_CHANNEL` ở channel level. Hoặc gộp vào `MANAGE_SERVER`. Bro cần confirm.

**Request body:**
```
{
  "name": "announcements",
  "isPrivate": false
}
```

Khi `isPrivate = true` → hệ thống tự tạo `channel_role_permissions` record: `(channelId, everyoneRoleId, VIEW_CHANNEL, DENY)`.

**Response 201:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "ch_002",
    "serverId": "srv_abc123",
    "name": "announcements",
    "isPrivate": true,
    "createdAt": "2025-06-01T10:00:00Z"
  }
}
```

> `isPrivate` trong response là computed: derive bằng cách check `VIEW_CHANNEL` của `@everyone` trong `channel_role_permissions`.

**Lỗi:**
- `403 FORBIDDEN` — không có quyền tạo channel.
- `409 CHANNEL_NAME_CONFLICT` — tên channel đã tồn tại trong server.

---

### GET /servers/:serverId/channels

Lấy danh sách channel trong server mà current user có thể thấy (có quyền `VIEW_CHANNEL`).

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": [
    {
      "id": "ch_001",
      "name": "general",
      "isPrivate": false,
      "createdAt": "2025-06-01T10:00:00Z"
    },
    {
      "id": "ch_002",
      "name": "staff-only",
      "isPrivate": true,
      "createdAt": "2025-06-01T10:00:00Z"
    }
  ]
}
```

---

### PATCH /servers/:serverId/channels/:channelId

Đổi tên channel. Yêu cầu `MANAGE_CHANNEL` ở channel-level.

**Request body:**
```
{
  "name": "new-channel-name"
}
```

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "id": "ch_002",
    "name": "new-channel-name"
  }
}
```

---

### DELETE /servers/:serverId/channels/:channelId

Xóa channel. Yêu cầu `MANAGE_CHANNEL` ở channel-level. Không thể xóa nếu đây là channel duy nhất còn lại trong server.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `409 LAST_CHANNEL` — không thể xóa channel cuối cùng trong server.

---

## US37 — Invite thành viên vào private channel

### POST /servers/:serverId/channels/:channelId/members

Mời thành viên server vào private channel. Yêu cầu `INVITE_MEMBERS` ở channel-level.

**Request body:**
```
{
  "userId": "usr_member_456"
}
```

Hệ thống tạo record trong `channel_user_permissions`: `(channelId, serverMemberId, VIEW_CHANNEL, ALLOW)`.

**Response 201:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `403 FORBIDDEN` — không có quyền `INVITE_MEMBERS`.
- `404 MEMBER_NOT_FOUND` — user không phải thành viên server.
- `409 ALREADY_HAS_ACCESS` — user đã có quyền xem channel này.

---

### GET /servers/:serverId/channels/:channelId/members

Lấy danh sách thành viên có quyền xem channel (chỉ meaningful với private channel). Yêu cầu `MANAGE_CHANNEL_PERMISSIONS`.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": [
    {
      "userId": "usr_xyz",
      "displayName": "Edward",
      "avatar": "https://cdn.example.com/...",
      "grantedVia": "USER_PERMISSION" // "USER_PERMISSION" | "ROLE" | "EVERYONE"
    }
  ]
}
```

---

## US38 — Thoát server

### DELETE /servers/:serverId/members/me

Current user tự rời server.

**Business rule:** Nếu current user là Owner → phải chuyển Owner trước (`PATCH /servers/:serverId/owner`). Nếu chưa chuyển → trả lỗi.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `409 OWNER_MUST_TRANSFER` — Owner chưa chuyển quyền trước khi rời.

---

## US38b — Cấu hình permission cho channel

### GET /servers/:serverId/channels/:channelId/permissions

Lấy toàn bộ permission config của channel (cả role-level và user-level). Yêu cầu `MANAGE_CHANNEL_PERMISSIONS`.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": {
    "rolePermissions": [
      {
        "roleId": "role_everyone",
        "roleName": "@everyone",
        "permissions": {
          "VIEW_CHANNEL": "ALLOW",
          "SEND_MESSAGES": "ALLOW",
          "MANAGE_MESSAGES": "DENY"
        }
      }
    ],
    "userPermissions": [
      {
        "userId": "usr_xyz",
        "displayName": "Edward",
        "permissions": {
          "MANAGE_CHANNEL": "ALLOW"
        }
      }
    ]
  }
}
```

---

### PUT /servers/:serverId/channels/:channelId/permissions/roles/:roleId

Set permission cho một role trong channel. Yêu cầu `MANAGE_CHANNEL_PERMISSIONS`.

**Request body:**
```
{
  "permissions": {
    "VIEW_CHANNEL": "ALLOW",
    "SEND_MESSAGES": "DENY"
  }
}
```

> PUT thay vì PATCH vì mỗi lần gọi sẽ upsert toàn bộ permission của role đó trong channel (delete old rows + insert new). Nếu muốn granular hơn thì dùng PATCH từng key — nhưng PUT đơn giản hơn cho UI permission editor.

**Special case — bật `VIEW_CHANNEL` cho `@everyone`:** Response trả thêm field `requiresConfirmation: true` và chưa apply ngay. Client phải gọi lại với `"confirm": true` trong body.

```
// Request khi cần confirm
{
  "permissions": { "VIEW_CHANNEL": "ALLOW" },
  "confirm": true
}
```

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

**Lỗi:**
- `409 REQUIRES_CONFIRMATION` — bật `VIEW_CHANNEL` cho `@everyone` mà không có `confirm: true`.

---

### PUT /servers/:serverId/channels/:channelId/permissions/users/:userId

Set permission cho một user cụ thể trong channel. Yêu cầu `MANAGE_CHANNEL_PERMISSIONS`.

**Request body:**
```
{
  "permissions": {
    "SEND_MESSAGES": "DENY"
  }
}
```

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

---

### DELETE /servers/:serverId/channels/:channelId/permissions/roles/:roleId

Xóa toàn bộ permission override của role trong channel (về inherit).

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

---

### DELETE /servers/:serverId/channels/:channelId/permissions/users/:userId

Xóa toàn bộ permission override của user trong channel.

**Response 200:**
```
{
  "code": "SUCCESS",
  "data": null
}
```

---

## Feedback & Gaps từ ERD

### 1. Thiếu bảng `server_bans`
ERD hiện tại không có bảng lưu danh sách user bị ban. Cần thêm để enforce khi user cố join lại bằng invite link hoặc direct invite.

Đề xuất:
```sql
server_bans (
  id          VARCHAR(36) PK,
  server_id   VARCHAR(36) FK -> servers.id,
  user_id     VARCHAR(36) FK -> users.id,
  banned_by   VARCHAR(36) FK -> users.id,
  reason      TEXT,
  created_at  DATETIME
)
-- Unique: (server_id, user_id)
```

### 2. Thiếu bảng `server_invitations`
US33 (direct invite) cần bảng riêng để track trạng thái accept/decline, khác với `invite_links` (link-based).

Đề xuất:
```sql
server_invitations (
  id          VARCHAR(36) PK,
  server_id   VARCHAR(36) FK -> servers.id,
  inviter_id  VARCHAR(36) FK -> users.id,
  invitee_id  VARCHAR(36) FK -> users.id,
  status      ENUM(PENDING, ACCEPTED, DECLINED),
  expires_at  DATETIME,
  created_at  DATETIME
)
-- Unique: (server_id, invitee_id) WHERE status = PENDING
```

### 3. Server permission `MANAGE_CHANNELS` còn thiếu
US41 không có permission nào cover việc **tạo channel**. `MANAGE_CHANNEL` là channel-level permission (US42), không phải server-level. Đề xuất thêm:

```
server_permission_key:
  + MANAGE_CHANNELS  // tạo/xóa channel trong server
```

Hoặc gộp vào `MANAGE_SERVER` nếu muốn đơn giản hơn — nhưng như vậy Moderator không thể manage channel mà không có toàn quyền server.

### 4. Mute cần làm rõ implementation
Hiện `MUTE_MEMBER` là server permission nhưng ERD không có chỗ lưu trạng thái mute. Hai hướng:

- **Đơn giản:** Thêm `is_muted BOOLEAN DEFAULT false` vào `server_members` → check khi user gửi message.
- **Phức tạp hơn:** Dùng `channel_user_permissions` để set `SEND_MESSAGES = DENY` cho từng channel — nhưng nặng hơn và không nhất quán với `MUTE_MEMBER` là server-level action.

Đề xuất V1: dùng `is_muted` trên `server_members`.

### 5. `isPrivate` của channel là derived field
Không lưu trong DB mà derive từ `channel_role_permissions` (check `VIEW_CHANNEL` của `@everyone`). Cần đảm bảo query này được cache/optimize tốt vì dùng nhiều (render sidebar channel list).