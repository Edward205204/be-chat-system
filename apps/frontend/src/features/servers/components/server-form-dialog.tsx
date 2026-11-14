import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { ImageField } from '@/components/common/image-field'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import type { ServerResponse } from '@/types/api'
import { useCreateServerMutation, useUpdateServerMutation } from '../queries'
import { serverFormSchema, type ServerFormValues } from '../schemas'

interface ServerFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  /** Có server → chế độ sửa, không có → tạo mới. */
  server?: ServerResponse
}

export function ServerFormDialog({
  open,
  onOpenChange,
  server
}: ServerFormDialogProps) {
  const navigate = useNavigate()
  const createServer = useCreateServerMutation()
  const updateServer = useUpdateServerMutation(server?.id ?? '')
  const form = useForm<ServerFormValues>({
    resolver: zodResolver(serverFormSchema),
    values: {
      name: server?.name ?? '',
      avatar: server?.avatar ?? '',
      banner: server?.banner ?? ''
    }
  })
  const { errors } = form.formState
  const isPending = createServer.isPending || updateServer.isPending

  const close = () => {
    form.reset()
    onOpenChange(false)
  }

  const handleSubmit = form.handleSubmit(({ name, avatar, banner }) => {
    if (!server) {
      return createServer.mutate(
        { name, avatar: avatar || undefined },
        {
          onSuccess: (created) => {
            close()
            navigate({
              to: '/servers/$serverId',
              params: { serverId: created.id }
            })
          }
        }
      )
    }
    // BE so trùng tên với chính server đang sửa (bug #8) → giữ tên cũ là bị 409.
    if (name === server.name)
      return form.setError('name', {
        message: 'BE yêu cầu đổi tên khi sửa server (bug #8)'
      })
    updateServer.mutate(
      { name, avatar: avatar || undefined, banner: banner || undefined },
      {
        onSuccess: () => {
          toast.success('Đã cập nhật server')
          close()
        }
      }
    )
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{server ? 'Sửa server' : 'Tạo server mới'}</DialogTitle>
        </DialogHeader>
        <form id='server-form' onSubmit={handleSubmit} className='grid gap-4'>
          <Controller
            control={form.control}
            name='avatar'
            render={({ field, fieldState }) => (
              <ImageField
                label='Ảnh server'
                value={field.value}
                onChange={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
          {server && (
            <Controller
              control={form.control}
              name='banner'
              render={({ field, fieldState }) => (
                <ImageField
                  label='Ảnh bìa'
                  variant='banner'
                  value={field.value}
                  onChange={field.onChange}
                  error={fieldState.error?.message}
                />
              )}
            />
          )}
          <Field>
            <FieldLabel htmlFor='server-name'>Tên server</FieldLabel>
            <Input id='server-name' {...form.register('name')} />
            <FieldError>{errors.name?.message}</FieldError>
          </Field>
        </form>
        <DialogFooter>
          <Button type='submit' form='server-form' disabled={isPending}>
            {server ? 'Lưu' : 'Tạo'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
