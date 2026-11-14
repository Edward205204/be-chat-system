import { useState, type FormEvent } from 'react'
import { useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { extractInviteToken } from '../api'
import { useJoinServerMutation } from '../queries'

interface JoinServerDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function JoinServerDialog({
  open,
  onOpenChange
}: JoinServerDialogProps) {
  const navigate = useNavigate()
  const joinServer = useJoinServerMutation()
  const [value, setValue] = useState('')

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    const token = extractInviteToken(value)
    if (!token) return
    joinServer.mutate(token, {
      onSuccess: (res) => {
        toast.success(`Đã vào ${res.serverName}`)
        setValue('')
        onOpenChange(false)
        navigate({
          to: '/servers/$serverId',
          params: { serverId: res.serverId }
        })
      }
    })
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tham gia server</DialogTitle>
          <DialogDescription>Dán link mời hoặc mã mời.</DialogDescription>
        </DialogHeader>
        <form id='join-form' onSubmit={handleSubmit}>
          <Input
            autoFocus
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder='http://localhost:3000/invite/...'
          />
        </form>
        <DialogFooter>
          <Button
            type='submit'
            form='join-form'
            disabled={!value.trim() || joinServer.isPending}
          >
            Tham gia
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
