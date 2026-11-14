import { z } from 'zod'
import { displayNameSchema, usernameSchema } from '@/features/auth/schemas'

const optionalUrl = z.union([
  z.literal(''),
  z.url('Phải là URL tuyệt đối (http/https)')
])

export const profileSchema = z.object({
  displayName: displayNameSchema,
  username: usernameSchema,
  avatar: optionalUrl,
  banner: optionalUrl,
  dateOfBirth: z.union([z.literal(''), z.iso.date('Ngày không hợp lệ')])
})

export type ProfileValues = z.infer<typeof profileSchema>
