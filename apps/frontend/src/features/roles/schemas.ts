import { z } from 'zod'

export const roleFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(3, 'Tên role 3-20 ký tự')
    .max(20, 'Tên role 3-20 ký tự'),
  color: z.string().regex(/^#[0-9A-Fa-f]{6}$/, 'Màu dạng #RRGGBB')
})

export type RoleFormValues = z.infer<typeof roleFormSchema>
