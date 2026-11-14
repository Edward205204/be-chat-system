import { z } from 'zod'

/** `existingNames`: BE kiểm trùng tên bị lỗi ra 500 (bug #5) → kiểm ở FE. */
export const channelFormSchema = (existingNames: string[]) =>
  z.object({
    name: z
      .string()
      .trim()
      .min(1, 'Bắt buộc')
      .max(100, 'Tối đa 100 ký tự')
      .refine((n) => !existingNames.includes(n), 'Tên channel đã tồn tại'),
    private: z.boolean()
  })

export type ChannelFormValues = z.infer<ReturnType<typeof channelFormSchema>>
