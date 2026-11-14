import { Controller, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from '@tanstack/react-router'
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
import { Switch } from '@/components/ui/switch'
import type { ChannelResponse } from '@/types/api'
import {
  useChannelsQuery,
  useCreateChannelMutation,
  useRenameChannelMutation
} from '../queries'
import { channelFormSchema, type ChannelFormValues } from '../schemas'

interface ChannelFormDialogProps {
  serverId: string
  open: boolean
  onOpenChange: (open: boolean) => void
  /** Có channel → đổi tên, không có → tạo mới. */
  channel?: ChannelResponse
}

export function ChannelFormDialog({
  serverId,
  open,
  onOpenChange,
  channel
}: ChannelFormDialogProps) {
  const navigate = useNavigate()
  const { data: channels = [] } = useChannelsQuery(serverId)
  const createChannel = useCreateChannelMutation(serverId)
  const renameChannel = useRenameChannelMutation(serverId)
  const existingNames = channels
    .filter((c) => c.id !== channel?.id)
    .map((c) => c.name)
  const form = useForm<ChannelFormValues>({
    resolver: zodResolver(channelFormSchema(existingNames)),
    values: { name: channel?.name ?? '', private: channel?.private ?? false }
  })
  const { errors } = form.formState

  const close = () => {
    form.reset()
    onOpenChange(false)
  }

  const handleSubmit = form.handleSubmit((values) => {
    if (channel)
      return renameChannel.mutate(
        { channelId: channel.id, name: values.name },
        { onSuccess: close }
      )
    createChannel.mutate(
      { name: values.name, isPrivate: values.private },
      {
        onSuccess: (created) => {
          close()
          navigate({
            to: '/servers/$serverId/$channelId',
            params: { serverId, channelId: created.id }
          })
        }
      }
    )
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {channel ? 'Đổi tên channel' : 'Tạo channel'}
          </DialogTitle>
        </DialogHeader>
        <form id='channel-form' onSubmit={handleSubmit} className='grid gap-4'>
          <Field>
            <FieldLabel htmlFor='channel-name'>Tên channel</FieldLabel>
            <Input id='channel-name' autoFocus {...form.register('name')} />
            <FieldError>{errors.name?.message}</FieldError>
          </Field>
          {!channel && (
            <Controller
              control={form.control}
              name='private'
              render={({ field }) => (
                <FieldLabel>
                  <Switch
                    checked={field.value}
                    onCheckedChange={field.onChange}
                  />
                  Channel riêng tư (chỉ owner và người được thêm)
                </FieldLabel>
              )}
            />
          )}
        </form>
        <DialogFooter>
          <Button
            type='submit'
            form='channel-form'
            disabled={createChannel.isPending || renameChannel.isPending}
          >
            {channel ? 'Lưu' : 'Tạo'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
