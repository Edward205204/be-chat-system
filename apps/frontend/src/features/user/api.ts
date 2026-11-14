import { request } from '@/lib/http'
import type {
  UserPatchUpdateRequest,
  UserPublicResponse,
  UserResponse
} from '@/types/api'

export const userApi = {
  getMe: () => request<UserResponse>({ url: '/users/me' }),
  updateMe: (body: UserPatchUpdateRequest) =>
    request<UserResponse>({ method: 'PATCH', url: '/users/me', data: body }),
  /** Khớp chính xác username/email. Không thấy thì trả undefined. */
  search: (q: string) =>
    request<UserResponse | undefined>({ url: '/users/search', params: { q } }),
  getById: (userId: string) =>
    request<UserPublicResponse>({ url: `/users/${userId}` })
}
