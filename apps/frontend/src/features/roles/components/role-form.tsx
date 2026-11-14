import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Button } from '@/components/ui/button'
import { FieldError } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { roleFormSchema, type RoleFormValues } from '../schemas'

interface RoleFormProps {
  defaultValues?: RoleFormValues
  submitLabel: string
  isPending: boolean
  onSubmit: (values: RoleFormValues, reset: () => void) => void
}

/** Dùng chung cho tạo và sửa role. */
export function RoleForm({
  defaultValues,
  submitLabel,
  isPending,
  onSubmit
}: RoleFormProps) {
  const form = useForm<RoleFormValues>({
    resolver: zodResolver(roleFormSchema),
    values: defaultValues ?? { name: '', color: '#5865F2' }
  })
  const { errors } = form.formState

  return (
    <form
      onSubmit={form.handleSubmit((v) => onSubmit(v, () => form.reset()))}
      className='grid gap-1'
    >
      <div className='flex gap-2'>
        <input
          type='color'
          aria-label='Màu role'
          className='h-9 w-10 shrink-0 cursor-pointer rounded-md border bg-transparent p-1'
          {...form.register('color')}
        />
        <Input placeholder='Tên role' {...form.register('name')} />
        <Button type='submit' variant='secondary' disabled={isPending}>
          {submitLabel}
        </Button>
      </div>
      <FieldError>{errors.name?.message ?? errors.color?.message}</FieldError>
    </form>
  )
}
