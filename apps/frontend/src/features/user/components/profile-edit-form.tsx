import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { ImageField } from '@/components/common/image-field'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import type { UserPatchUpdateRequest, UserResponse } from '@/types/api'
import { useUpdateMeMutation } from '../queries'
import { profileSchema, type ProfileValues } from '../schemas'

interface ProfileEditFormProps {
  user: UserResponse
  onDone: () => void
}

/** Chỉ dùng cho hồ sơ của chính mình (PATCH /users/me). */
export function ProfileEditForm({ user, onDone }: ProfileEditFormProps) {
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
    if (Object.keys(body).length === 0) return onDone()
    updateMe.mutate(body, {
      onSuccess: () => {
        toast.success('Đã cập nhật hồ sơ')
        onDone()
      }
    })
  })

  return (
    <form
      onSubmit={handleSubmit}
      className='grid gap-4 rounded-xl border bg-card p-6'
    >
      <div className='grid gap-4 sm:grid-cols-[auto_1fr]'>
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
      </div>
      <div className='grid gap-4 sm:grid-cols-2'>
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
      </div>
      <div className='flex justify-end gap-2'>
        <Button type='button' variant='ghost' onClick={onDone}>
          Huỷ
        </Button>
        <Button type='submit' disabled={updateMe.isPending}>
          Lưu
        </Button>
      </div>
    </form>
  )
}
