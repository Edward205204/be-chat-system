import { useState } from 'react'
import { useNavigate } from '@tanstack/react-router'
import {
  ChevronDownIcon,
  LogOutIcon,
  PencilIcon,
  SettingsIcon,
  Trash2Icon,
  UserPlusIcon
} from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import type { ServerResponse } from '@/types/api'
import { useDeleteServerMutation, useLeaveServerMutation } from '../queries'
import { InviteDialog } from './invite-dialog'
import { ServerFormDialog } from './server-form-dialog'
import { ServerSettingsDialog } from './server-settings-dialog'

type OpenDialog = 'invite' | 'edit' | 'settings' | null

export function ServerMenu({ server }: { server: ServerResponse }) {
  const navigate = useNavigate()
  const [dialog, setDialog] = useState<OpenDialog>(null)
  const deleteServer = useDeleteServerMutation()
  const leaveServer = useLeaveServerMutation()
  const goHome = () => navigate({ to: '/' })

  const handleDelete = () => {
    if (
      confirm(
        `Xoá server "${server.name}"? Toàn bộ channel và tin nhắn sẽ mất.`
      )
    )
      deleteServer.mutate(server.id, { onSuccess: goHome })
  }

  const handleLeave = () => {
    if (confirm(`Rời server "${server.name}"?`))
      leaveServer.mutate(server.id, { onSuccess: goHome })
  }

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger className='flex h-12 w-full cursor-pointer items-center justify-between border-b px-4 font-semibold hover:bg-accent'>
          <span className='truncate'>{server.name}</span>
          <ChevronDownIcon className='size-4' />
        </DropdownMenuTrigger>
        <DropdownMenuContent className='w-56'>
          <DropdownMenuItem onClick={() => setDialog('invite')}>
            <UserPlusIcon /> Mời bạn bè
          </DropdownMenuItem>
          {server.owner && (
            <>
              <DropdownMenuItem onClick={() => setDialog('edit')}>
                <PencilIcon /> Sửa server
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setDialog('settings')}>
                <SettingsIcon /> Role, link mời, ban
              </DropdownMenuItem>
            </>
          )}
          <DropdownMenuSeparator />
          {server.owner ? (
            <DropdownMenuItem variant='destructive' onClick={handleDelete}>
              <Trash2Icon /> Xoá server
            </DropdownMenuItem>
          ) : (
            // Owner rời server làm server mồ côi (bug #18) → chỉ member thường được rời.
            <DropdownMenuItem variant='destructive' onClick={handleLeave}>
              <LogOutIcon /> Rời server
            </DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>
      <InviteDialog
        serverId={server.id}
        open={dialog === 'invite'}
        onOpenChange={(o) => !o && setDialog(null)}
      />
      {server.owner && (
        <ServerSettingsDialog
          server={server}
          open={dialog === 'settings'}
          onOpenChange={(o) => !o && setDialog(null)}
        />
      )}
      <ServerFormDialog
        server={server}
        open={dialog === 'edit'}
        onOpenChange={(o) => !o && setDialog(null)}
      />
    </>
  )
}
