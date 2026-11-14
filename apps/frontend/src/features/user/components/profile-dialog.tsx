import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
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
import type { UserPatchUpdateRequest, UserResponse } from '@/types/api'
import { useUpdateMeMutation } from '../queries'
import { profileSchema, type ProfileValues } from '../schemas'

interface ProfileDialogProps {
  user: UserResponse
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ProfileDialog({
  user,
  open,
  onOpenChange
}: ProfileDialogProps) {
  const updateMe = useUpdateMeMutation()
  const form = useForm<ProfileValues>({
    resolver: zodResolver(profileSchema),
    values: {
      displayName: user.displayName,
      username: user.username,
      avatar: user.avatar ?? '',
      banner: user.banner ?? '',
      dateOfBirth: user.dateOfBirth ?? ''
    }
  })
  const { errors } = form.formState

  const handleSubmit = form.handleSubmit((values) => {
    // Chỉ gửi field thay đổi: gửi lại username cũ bị USERNAME_EXISTED (bug #9).
    // BE không cho xoá ảnh về null → bỏ trống thì giữ nguyên.
    const body: UserPatchUpdateRequest = {}
    if (values.displayName !== user.displayName)
      body.displayName = values.displayName
    if (values.username !== user.username) body.username = values.username
    if (values.avatar && values.avatar !== user.avatar)
      body.avatar = values.avatar
    if (values.banner && values.banner !== user.banner)
      body.banner = values.banner
    if (values.dateOfBirth && values.dateOfBirth !== user.dateOfBirth)
      body.dateOfBirth = values.dateOfBirth
    if (Object.keys(body).length === 0) return onOpenChange(false)
    updateMe.mutate(body, {
      onSuccess: () => {
        toast.success('Đã cập nhật hồ sơ')
        onOpenChange(false)
      }
    })
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90svh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Hồ sơ của bạn</DialogTitle>
        </DialogHeader>
        <form id='profile-form' onSubmit={handleSubmit} className='grid gap-4'>
          <Controller
            control={form.control}
            name='avatar'
            render={({ field, fieldState }) => (
              <ImageField
                label='Avatar'
                value={field.value}
                onChange={field.onChange}
                error={fieldState.error?.message}
              />
            )}
          />
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
          <Field>
            <FieldLabel htmlFor='displayName'>Tên hiển thị</FieldLabel>
            <Input id='displayName' {...form.register('displayName')} />
            <FieldError>{errors.displayName?.message}</FieldError>
          </Field>
          <Field>
            <FieldLabel htmlFor='username'>Username</FieldLabel>
            <Input id='username' {...form.register('username')} />
            <FieldError>{errors.username?.message}</FieldError>
          </Field>
          <Field>
            <FieldLabel htmlFor='dateOfBirth'>Ngày sinh</FieldLabel>
            <Input
              id='dateOfBirth'
              type='date'
              {...form.register('dateOfBirth')}
            />
            <FieldError>{errors.dateOfBirth?.message}</FieldError>
          </Field>
          <p className='text-xs text-muted-foreground'>Email: {user.email}</p>
        </form>
        <DialogFooter>
          <Button
            type='submit'
            form='profile-form'
            disabled={updateMe.isPending}
          >
            Lưu
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
