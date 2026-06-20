# Đặc tả Yêu cầu Chức năng
**Ứng dụng Chat & Server (Discord-like) — Phiên bản 1.1**

---

## Ghi chú chung

- Tất cả màn hình Setting (server, channel, role) đều áp dụng **lazy apply**: thay đổi chỉ có hiệu lực khi bấm nút **Lưu**. Nút Lưu chỉ hiển thị khi có ít nhất một thay đổi chưa được lưu.
- Permission hoạt động theo thứ tự ưu tiên: **User permission > Role permission > @everyone permission**.

---

## Epic 1: Xác thực (Auth)

### US11 — Đăng ký tài khoản

> Với tư cách là **người dùng mới**, tôi muốn đăng ký tài khoản bằng email.

**Form đăng ký gồm:**
- Email (bắt buộc, unique)
- Display name (bắt buộc, không unique)
- Username (bắt buộc, unique — chỉ chứa chữ thường, số, dấu gạch dưới)
- Password (tối thiểu 8 ký tự, ít nhất 1 chữ hoa, 1 chữ thường, 1 ký tự đặc biệt)
- Ngày sinh (ngày / tháng / năm)
- Nút "Tạo tài khoản"

**Quy tắc nghiệp vụ:**
- Email đã tồn tại và **đã verify** → báo lỗi "Email đã được sử dụng", không cho đăng ký.
- Email đã tồn tại nhưng **chưa verify** → cho phép ghi đè toàn bộ thông tin (email, display name, username, password, ngày sinh) bằng dữ liệu mới. OTP cũ bị vô hiệu hóa.
- Username đã tồn tại (dù tài khoản đó verify hay chưa) → báo lỗi "Username đã được sử dụng".

---

### US12 — Xác thực email bằng OTP

> Với tư cách là **người dùng vừa đăng ký**, tôi muốn xác thực email bằng mã OTP để đảm bảo email là của chính mình.

**Màn hình xác thực gồm:**
- Ô nhập mã OTP (6 chữ số)
- Nút "Xác nhận"
- Nút "Gửi lại mã" — cooldown 60 giây, đếm ngược hiển thị trực tiếp trên nút

**Quy tắc nghiệp vụ:**
- OTP có hiệu lực trong 10 phút kể từ lúc gửi.
- Khi gửi lại mã, OTP cũ bị vô hiệu hóa ngay lập tức.
- Nếu đóng trang hoặc logout trước khi verify → lần đăng nhập tiếp theo bằng tài khoản chưa verify sẽ tự động hiển thị lại màn hình này.

> **Out of scope v1:** Xác thực qua SMS hoặc authenticator app.

---

### US13 — Đăng nhập

> Với tư cách là **người dùng đã có tài khoản**, tôi muốn đăng nhập bằng email và password.

**Form đăng nhập gồm:**
- Email, Password, nút "Đăng nhập"
- Link dẫn đến trang Quên mật khẩu

**Quy tắc nghiệp vụ:**
- Đăng nhập với tài khoản **chưa verify** → redirect đến màn hình xác thực OTP (US12), không vào app.
- Sai email hoặc password → báo lỗi chung "Email hoặc mật khẩu không đúng" (không tiết lộ cái nào sai).

---

### US14 — Quên mật khẩu

> Với tư cách là **người dùng**, tôi muốn đặt lại mật khẩu khi quên để lấy lại quyền truy cập.

**Luồng 3 màn hình:**
1. Nhập email → nút "Gửi mã xác nhận"
2. Nhập OTP (tương tự US12: cooldown 60s, hết hạn sau 10 phút)
3. Nhập password mới + xác nhận → nút "Đặt lại mật khẩu"

**Quy tắc nghiệp vụ:**
- Nếu email không tồn tại → vẫn hiển thị "Nếu email tồn tại, mã sẽ được gửi" (tránh lộ thông tin).
- Sau khi đặt lại mật khẩu thành công → đăng xuất tất cả phiên hiện tại, chuyển về trang đăng nhập.

---

## Epic 2: Tin nhắn trực tiếp (Direct Message)

### US21 — Tìm kiếm người dùng

> Với tư cách là **người dùng**, tôi muốn tìm kiếm người dùng khác bằng username hoặc email để gửi kết bạn hoặc nhắn tin.

**Quy tắc nghiệp vụ:**
- Tìm kiếm exact match với username hoặc email.
- Không hiển thị kết quả của chính mình.
- Không tìm thấy → hiển thị "Không tìm thấy người dùng".

---

### US22 — Gửi và quản lý kết bạn

> Với tư cách là **người dùng**, tôi muốn gửi, chấp nhận, từ chối và hủy kết bạn để quản lý danh sách bạn bè.

**Trạng thái quan hệ:**
- `stranger` — chưa có tương tác kết bạn.
- `pending_sent` — mình đã gửi lời mời, chờ đối phương phản hồi.
- `pending_received` — đối phương gửi, chờ mình phản hồi.
- `friends` — đã chấp nhận kết bạn.

**Quy tắc nghiệp vụ:**
- Mỗi cặp người dùng chỉ có một lời mời tồn tại tại một thời điểm.
- Người gửi có thể hủy lời mời trước khi đối phương phản hồi.
- Cả hai có thể hủy kết bạn sau khi đã là bạn bè.

> **Out of scope v1:** Chặn người dùng (block/unblock).

---

### US23 — Danh sách bạn bè và lịch sử nhắn tin

> Với tư cách là **người dùng**, tôi muốn xem danh sách bạn bè và những người mình đã nhắn tin để nhanh chóng mở lại cuộc trò chuyện.

**Hai danh sách riêng biệt trong sidebar:**
- **Danh sách Bạn bè:** chỉ hiển thị người đã kết bạn.
- **Danh sách DM gần đây:** tất cả cuộc trò chuyện DM (kể cả người lạ), sắp xếp theo thời gian tin nhắn gần nhất.

**Mỗi mục hiển thị:** avatar, display name (không phải username), trạng thái hoạt động (online/offline/away), label "Bạn bè" hoặc "Người lạ".

> Người dùng **có thể nhắn tin với người lạ** mà không cần kết bạn — xem US25. Kết bạn chỉ phục vụ mục đích tổ chức danh sách và invite server.

---

### US24 — Profile sidebar khi mở DM

> Với tư cách là **người dùng**, tôi muốn xem profile của người mình đang nhắn tin ở sidebar phải.

**Nội dung sidebar phải:**
- Banner/thumbnail (ảnh cover, có thể để trống)
- Avatar
- Display name (in đậm, font lớn hơn)
- Username (dạng `@username`)
- Ngày tạo tài khoản
- Nhóm chung: tối đa 5 server gần nhất, nếu nhiều hơn hiển thị "+N nhóm chung"
- Bạn chung: tối đa 5 người, nếu nhiều hơn hiển thị "+N bạn chung"

---

### US25 — Nhắn tin trực tiếp

> Với tư cách là **người dùng**, tôi muốn nhắn tin với bất kỳ người dùng nào tìm được hoặc đã là bạn bè.

**Quy tắc nghiệp vụ:**
- Không cần kết bạn để nhắn tin (DM mở với tất cả người dùng trong hệ thống).
- Mở DM lần đầu với người lạ → tạo conversation mới, xuất hiện trong DM list của cả hai.

> **Out of scope v1:** Chặn DM từ người lạ, xóa conversation, tin nhắn media/file, reaction, reply.

---

## Epic 3: Server

### US31 — Tạo server

> Với tư cách là **người dùng**, tôi muốn tạo một server mới.

**Luồng:**
1. Bấm nút "+" ở sidebar → popup 2 lựa chọn:
   - "Tạo server của riêng bạn" → mở form tạo server
   - "Tham gia server bằng link" → mở ô nhập link (xem US34)

**Form tạo server:**
- Upload avatar server (tùy chọn)
- Tên server (bắt buộc)
- Nút "Tạo"

**Quy tắc nghiệp vụ:**
- Tạo xong → server có sẵn 1 channel mặc định tên `general` (có thể xóa bình thường).
- Người tạo tự động trở thành Owner với toàn quyền.

---

### US32 — Cài đặt server

> Với tư cách là **chủ server (Owner)**, tôi muốn chỉnh sửa thông tin và cấu hình server.

**Các mục cài đặt:**
- Tên server
- Avatar server (icon tròn hiển thị ở sidebar)
- Thumbnail server (banner hiển thị ở đầu trang server, có thể để trống)
- Quản lý roles và permission (xem Epic 4)

---

### US33 — Mời bạn bè trực tiếp vào server

> Với tư cách là **thành viên có quyền Tạo invite**, tôi muốn mời bạn bè vào server trực tiếp mà không cần chia sẻ link.

**Cách mời:**
- Tìm theo username trong thanh tìm kiếm
- Chọn từ danh sách bạn bè
- Chọn từ danh sách DM gần đây

**Quy tắc nghiệp vụ:**
- Người được mời nhận thông báo, có thể chấp nhận hoặc từ chối.

---

### US34 — Chia sẻ link mời server

> Với tư cách là **thành viên có quyền Tạo invite**, tôi muốn tạo và chia sẻ link mời để người khác join server.

**Quy tắc nghiệp vụ:**
- Mỗi link có thời hạn 3 ngày kể từ khi tạo.
- Không giới hạn số lượng người dùng link (số lượt join chỉ để thống kê, không phải enforcement).

> **Out of scope v1:** Tùy chỉnh thời hạn link, giới hạn số lượng người, gán role tự động khi join bằng link.

---

### US35 — Quản lý danh sách link mời

> Với tư cách là **Owner hoặc người có quyền Manage Server**, tôi muốn xem và quản lý toàn bộ link mời đã được tạo.

**Thông tin mỗi link:**
- Mã link (token)
- Người tạo (display name + avatar)
- Ngày tạo
- Trạng thái: còn hiệu lực (còn X ngày) / đã hết hạn / đã bị vô hiệu hóa
- Số lượng người đã join bằng link này
- Nút "Vô hiệu hóa" (chỉ hiện khi link còn hiệu lực)

> **Cân nhắc v2:** Kick hàng loạt các thành viên đã join bằng một link cụ thể.

---

### US36 — Tạo channel

> Với tư cách là **thành viên có quyền Manage Channel**, tôi muốn tạo channel mới trong server.

**Form tạo channel:**
- Tên channel (bắt buộc)
- Loại: Public hoặc Private
- Nút "Tạo channel"

**Quy tắc nghiệp vụ:**
- **Public channel:** tất cả thành viên server đều thấy và vào được.
- **Private channel:** permission "View Channel" của role `@everyone` bị tắt ngay khi tạo. Chỉ người được invite trực tiếp mới thấy channel.
- Nếu sau này bật lại "View Channel" cho `@everyone` trong private channel → hệ thống hiện confirmation: *"Bật quyền này sẽ chuyển channel thành public. Xác nhận?"*

---

### US37 — Invite trực tiếp vào private channel

> Với tư cách là **người có quyền Invite Members vào channel**, tôi muốn mời thành viên server vào private channel.

**Quy tắc nghiệp vụ:**
- Chỉ có thể mời người đã là thành viên server (không mời người ngoài thẳng vào channel).
- Người được mời sẽ thấy channel trong danh sách channel của server.

---

### US38 — Cấu hình permission cho channel

> Với tư cách là **người có quyền Manage Channel Permissions**, tôi muốn tùy chỉnh permission theo role hoặc user cho từng channel.

Xem chi tiết các permission tại US42.

---

## Epic 4: Roles và Permission

### US41 — Quản lý roles server

> Với tư cách là **chủ server (Owner)**, tôi muốn tạo, chỉnh sửa và xóa roles để phân quyền cho thành viên.

**Quản lý role:**
- Tạo role mới: đặt tên, chọn màu (tùy chọn)
- Thêm / xóa thành viên khỏi role
- Xóa role (không thể xóa `@everyone`)

**Danh sách server permission:**

| Permission | Mô tả |
|---|---|
| Tạo invite | Tạo link mời và mời trực tiếp vào server |
| Quản lý server | Thay đổi tên, avatar, thumbnail server |
| Kick thành viên | Đuổi thành viên ra khỏi server (có thể join lại bằng link) |
| Ban thành viên | Cấm vĩnh viễn, không thể join lại dù có link |
| Mute thành viên | Tắt quyền gửi tin nhắn trong toàn server |
| Quản lý roles | Assign/unassign roles cho thành viên khác, chỉnh permission của roles **thấp hơn** (không thể chỉnh role bằng hoặc cao hơn mình) |

> Owner luôn có toàn quyền, không bị ảnh hưởng bởi bất kỳ permission nào.

> **Out of scope v1:** Role hierarchy chi tiết (phân cấp theo thứ tự ưu tiên). V1 chỉ phân biệt Owner vs non-Owner.

---

### US42 — Cấu hình permission cho channel

> Với tư cách là **người có quyền Manage Channel Permissions**, tôi muốn cấu hình permission theo role hoặc user cho từng channel.

**Danh sách channel permission:**

| Permission | Mô tả |
|---|---|
| View Channel | Xem channel trong danh sách và đọc tin nhắn. Bật cho `@everyone` → channel thành public (có confirmation). Tắt cho `@everyone` → channel thành private. |
| Manage Channel | Đổi tên channel, xóa channel và các cài đặt khác |
| Manage Channel Permissions | Cấu hình permission cho channel này — tương đương Owner trong phạm vi channel |
| Invite Members | Mời thành viên server vào channel (chỉ có nghĩa với private channel) |
| Send Messages | Gửi tin nhắn trong channel |
| Manage Messages | Xóa tin nhắn của người khác, ghim/bỏ ghim tin nhắn |

**Thứ tự ưu tiên:** User permission > Role permission > `@everyone` permission.

Nếu user được grant permission tường minh (allow/deny) thì role permission không có hiệu lực với user đó cho channel này.

---

## Out of Scope — Phiên bản 1

Các tính năng dưới đây được ghi nhận nhưng **không** nằm trong phạm vi phát triển v1:

- Chặn người dùng (block/unblock)
- Notification (tin nhắn mới, kết bạn, invite server)
- Tin nhắn media: ảnh, file, video, GIF
- Reaction và reply tin nhắn
- Xóa conversation DM
- Xóa tài khoản
- Voice / video channel
- Role hierarchy chi tiết
- Tùy chỉnh thời hạn và giới hạn số lượng người dùng link mời
- Kick hàng loạt user join bằng một link cụ thể
- Gán role tự động khi join bằng link