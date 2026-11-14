# FE Conventions — fe_chat_system

Tra nhanh khi mất context. Đọc hết trước khi code. Đặc tả API: `apps/backend/docs/api/` (README + 01..07 + `types.ts`).

## 1. Stack (mỗi vấn đề MỘT thư viện)

| Vấn đề                        | Dùng                                                           | KHÔNG cài thêm                          |
| ----------------------------- | -------------------------------------------------------------- | --------------------------------------- |
| Build / dev server            | Vite 8 + `@vitejs/plugin-react` + React Compiler               | webpack, CRA                            |
| Ngôn ngữ                      | TypeScript strict                                              | JS thuần                                |
| Routing                       | `@tanstack/react-router` (file-based, `src/routes`)            | react-router, wouter                    |
| Server state (gọi API, cache) | `@tanstack/react-query`                                        | SWR, RTK Query, fetch trong useEffect   |
| Client state (global)         | `zustand` (+ `persist` khi cần lưu)                            | redux, jotai, recoil, Context làm store |
| HTTP                          | `axios` qua instance duy nhất `@/lib/http`                     | fetch rải rác, ky                       |
| Realtime                      | `@stomp/stompjs` qua `@/lib/stomp`                             | sockjs, socket.io                       |
| Form                          | `react-hook-form` + `@hookform/resolvers/zod`                  | formik, TanStack Form                   |
| Validation                    | `zod`                                                          | yup, joi                                |
| UI primitives                 | shadcn/ui (Base UI `@base-ui/react`) trong `src/components/ui` | MUI, AntD, Radix, Chakra                |
| Styling                       | Tailwind CSS v4 + `cn()` (`clsx` + `tailwind-merge`)           | CSS-in-JS, SCSS, styled-components      |
| Toast                         | `sonner`                                                       | react-toastify                          |
| Icon                          | `lucide-react`                                                 | react-icons, heroicons                  |
| Ngày giờ                      | `dayjs` qua `@/lib/date`                                       | moment, date-fns                        |
| Lint / format                 | ESLint (flat) + Prettier                                       | tslint, standard                        |

UI chỉ dùng shadcn: thêm component bằng `pnpm dlx shadcn@latest add <tên>` (vào `src/components/ui`), không cài UI lib khác.

Muốn thêm lib: kiểm bảng trên trước. Có lib cùng chức năng thì dùng cái đã có. Lib mới phải ghi vào bảng.

## 2. Kiến trúc thư mục (feature-based)

```
src/
  routes/            # chỉ định nghĩa route + guard, ghép component từ features. File mỏng.
  features/<name>/   # auth | user | servers | channels | chat | roles | notifications
    api.ts           # hàm gọi API thuần (axios), trả result đã unwrap
    queries.ts       # queryKeys + useQuery/useMutation hooks
    schemas.ts       # zod schema của form
    store.ts         # zustand store (nếu feature cần)
    components/      # component riêng của feature
  components/
    ui/              # shadcn primitives, không chứa logic nghiệp vụ
    common/          # component dùng chung nhiều feature (UserAvatar, EmptyState...)
  lib/               # http, stomp, date, errors, utils (không phụ thuộc feature)
  types/api.ts       # type API, copy từ docs/api/types.ts. Nguồn type duy nhất.
  styles/globals.css
```

Phiên đăng nhập (`lib/session.ts`, zustand persist) và kết nối STOMP (`lib/stomp.ts`) là hạ tầng chung nên ở `lib`, không ở feature.

Quy tắc phụ thuộc: `routes → features → components/lib/types`. Feature được import feature khác qua file public (`api.ts`, `queries.ts`, `store.ts`, `components/*`), không import ngược vào `routes`. `components/ui` và `lib` không import `features`.

## 3. Naming

- File/thư mục: `kebab-case` (`server-sidebar.tsx`, `use-chat.ts`). Route file theo quy ước TanStack (`$serverId.tsx`, `_app.tsx`).
- Component: `PascalCase`, named export (`export function ServerSidebar`). Không default export (trừ khi lib yêu cầu).
- Hook: `useXxx`. Query hook: `useServersQuery`, mutation: `useCreateServerMutation`.
- Hằng số: `UPPER_SNAKE_CASE`. Type/interface: `PascalCase`, không tiền tố `I`.
- Boolean: `isX`/`hasX`/`canX`. Handler: `handleX` (trong component), prop callback `onX`.
- Query key: factory trong `queries.ts`, vd `serverKeys.members(serverId)`.

## 4. Clean code

- Component ≤ ~150 dòng; quá thì tách. Một component một việc.
- Không gọi axios trong component. Component → hook (`queries.ts`) → `api.ts` → `http`.
- Không `any`. Type request/response lấy từ `@/types/api`.
- Lỗi API: luôn qua `getErrorMessage(err)` (`@/lib/errors`) rồi `toast.error`. Không hiện `message` thô của BE.
- Validate mọi form ở FE bằng zod đúng quy tắc trong spec (BE trả validation lỗi thành 401/500).
- Không hardcode URL BE; luôn dùng path tương đối `/rchat` qua proxy Vite.
- Import bằng alias `@/`, không `../../..`.
- Không comment thừa; chỉ comment chỗ né bug BE (ghi số bug trong `07-errors.md`).

## 5. Ảnh (avatar, banner, ảnh server)

- Mọi field ảnh dùng `ImageField` (`@/components/common/image-field`) qua RHF `Controller`. Mặc định upload file, ô URL ẩn sau link "Dùng URL thay thế". Không tự làm input ảnh riêng.
- Upload qua `uploadImage` (`@/lib/files`): kiểm loại PNG/JPG/WEBP/GIF và tối đa 1 MB, trả URL tuyệt đối (BE validate `@URL`).
- Hiển thị ảnh luôn qua `useImageSrc` hoặc `UserAvatar`: ảnh của BE cần token nên được tải dạng blob; `<img src>` trần sẽ bị 401.

## 6. Popup & danh sách (chống giật layout)

- Popup luôn dùng `DialogContent` với `size`: `sm` (mặc định, form ngắn, `max-w-md`) hoặc `lg` (`max-w-2xl`, cao cố định `min(40rem,80svh)`). Không tự đặt `max-w-*`/`max-h-*` riêng.
- Popup neo mép trên (`top-[10svh]`), không căn giữa dọc, để nội dung đổi chiều cao không làm popup nhảy.
- Popup `lg`: header đứng yên, phần cuộn bọc trong `DialogBody`. Có tab thì mỗi `TabsContent` tự cuộn (xem `server-settings-dialog.tsx`), nên chuyển tab không đổi kích thước.
- Vùng cuộn luôn có `[scrollbar-gutter:stable]`. Nội dung xuất hiện sau (kết quả tìm, lỗi) phải có chỗ giữ sẵn (`min-h-*`); đang tải thì hiện `Skeleton` cùng chiều cao.
- Mọi danh sách số lượng không cố định (ban, link mời, member, role, quyền…) dùng `LoadMoreList` (`@/components/common/load-more-list`): chiều cao cố định (`height`: `sm|md|lg|fill`), cuộn bên trong, có skeleton và empty state, nút "Tải thêm".
  - BE có phân trang → `useInfiniteQuery` (size `PAGE_SIZE` ở `features/servers/api.ts`) và truyền `remote={{ hasMore, isLoading, onLoadMore }}`. Đang dùng cho ban (offset) và link mời (cursor).
  - BE không phân trang hoặc đã tải hết → bỏ `remote`, list tự cắt ở client (`pageSize`, mặc định 10).

## 7. Format & lint

- Prettier: single quote, không semicolon, không trailing comma (`.prettierrc`).
- `pnpm lint` và `pnpm build` phải sạch trước khi xong việc. `pnpm format` để format.
- `src/routeTree.gen.ts` là file sinh tự động, không sửa tay.

## 8. Bẫy BE phải nhớ (chi tiết ở `docs/api/07-errors.md`)

- Login trả token phẳng; verify/refresh trả token lồng `{token}` → chuẩn hoá bằng `pickToken`.
- Boolean JSON bỏ `is`: `private`, `owner`, `muted`, `revoked`. Riêng request tạo channel phải gửi `isPrivate` (bắt buộc).
- `inviteUrl` sai → tự ghép `${origin}/invite/${token}`.
- Tin nhắn trang đầu là mới nhất, `nextCursor` dẫn tới tin cũ hơn; tin realtime dedupe theo `id`.
- `memberId` ≠ `userId` (kick/mute/role dùng memberId; ban/invite dùng userId).
- Không có realtime cho member/channel/lời mời → invalidate query sau mutation.
- Không có API xoá lời mời đã nhận → trang `/notifications` ẩn thông báo cục bộ (`features/notifications/store.ts`, localStorage).
- Chưa có API xem hồ sơ người khác → `/users/$userId` dùng `useUser`; bật `PUBLIC_PROFILE_ENABLED` (`features/user/constants.ts`) khi BE sẵn sàng.
