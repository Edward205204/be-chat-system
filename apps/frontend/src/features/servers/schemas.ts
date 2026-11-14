import { z } from 'zod'

const optionalUrl = z.union([z.literal(''), z.url('Phải là URL tuyệt đối')])

export const serverFormSchema = z.object({
  name: z.string().trim().min(1, 'Bắt buộc').max(100, 'Tối đa 100 ký tự'),
  avatar: optionalUrl,
  banner: optionalUrl
})

export type ServerFormValues = z.infer<typeof serverFormSchema>
