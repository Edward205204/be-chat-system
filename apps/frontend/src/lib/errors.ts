import { isAxiosError } from 'axios'
import type { ApiResponse } from '@/types/api'

export class ApiError extends Error {
  readonly code: string
  readonly httpStatus?: number

  constructor(code: string, message: string, httpStatus?: number) {
    super(message)
    this.code = code.trim() // 'ROLE_NAME_DUPLICATE ' có dấu cách cuối
    this.httpStatus = httpStatus
  }
}

export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) return error
  if (isAxiosError<ApiResponse<unknown>>(error)) {
    const body = error.response?.data
    if (body?.status)
      return new ApiError(
        body.status,
        body.message ?? '',
        error.response?.status
      )
    return new ApiError('NETWORK_ERROR', error.message, error.response?.status)
  }
  return new ApiError(
    'UNKNOWN',
    error instanceof Error ? error.message : String(error)
  )
}

const MESSAGES: Record<string, string> = {
  NETWORK_ERROR: 'Không kết nối được server. Backend có đang chạy không?',
  UNAUTHENTICATED: 'Phiên đăng nhập hết hạn hoặc dữ liệu không hợp lệ.',
  LOGIN_FAILED: 'Email hoặc mật khẩu không đúng.',
  UNAUTHORIZED: 'Bạn không có quyền thực hiện thao tác này.',
  NOT_A_MEMBER: 'Bạn không phải thành viên server này.',
  MISSING_PERMISSION: 'Bạn thiếu quyền trong channel này.',
  NOW_DO_NOT_HAVE_PERMISSION: 'Bạn không có quyền thực hiện thao tác này.',
  USER_BANNED: 'Bạn đã bị cấm khỏi server này.',
  EMAIL_NOT_FOUND: 'Email không tồn tại.',
  USER_NOT_FOUND: 'Không tìm thấy người dùng.',
  SERVER_NOT_EXIST: 'Server không tồn tại.',
  INVITE_LINK_NOT_FOUND: 'Link mời không tồn tại hoặc đã hết hạn.',
  DIRECT_INVITE_NOT_FOUND: 'Lời mời không tồn tại hoặc đã hết hạn.',
  DIRECT_INVITE_HAS_USED: 'Lời mời này đã được xử lý.',
  CHANNEL_IS_NOT_EXIST: 'Channel không tồn tại.',
  OTP_DOES_NOT_EXIST: 'Chưa có mã OTP, hãy gửi mã mới.',
  OTP_INCORRECT: 'Mã OTP không đúng.',
  OTP_EXPIRED: 'Mã OTP đã hết hạn.',
  OTP_HAS_BEEN_USED: 'Mã OTP đã được dùng.',
  OTP_REVOKED: 'Mã OTP đã bị thu hồi, hãy gửi mã mới.',
  OTP_MAX_ATTEMPTS_EXCEEDED: 'Sai OTP quá 5 lần, hãy gửi mã mới.',
  OTP_COOLDOWN: 'Vui lòng đợi 2 phút trước khi gửi lại mã.',
  EMAIL_EXISTED: 'Email đã được đăng ký.',
  USERNAME_EXISTED: 'Username đã có người dùng.',
  USER_VERIFIED: 'Tài khoản đã được xác thực.',
  USER_ALREADY_A_MEMBER: 'Người dùng đã là thành viên.',
  SERVER_NAME_DUPLICATE: 'Bạn đã có server trùng tên.',
  CHANNEL_NAME_DUPLICATE: 'Tên channel bị trùng.',
  DIRECT_INVITE_ALREADY_PENDING: 'Đã có lời mời đang chờ cho người này.',
  UNCATEGORIZED: 'Lỗi hệ thống, vui lòng thử lại.'
}

export function getErrorMessage(error: unknown): string {
  const apiError = toApiError(error)
  return MESSAGES[apiError.code] ?? (apiError.message || 'Đã có lỗi xảy ra.')
}
