import { useState } from 'react'
import { Link, useNavigate } from '@tanstack/react-router'
import {
  BellIcon,
  LinkIcon,
  LogOutIcon,
  MessageCircleIcon,
  PlusIcon,
  UserIcon
} from 'lucide-react'
import { UserAvatar } from '@/components/common/user-avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { Separator } from '@/components/ui/separator'
import { useLogoutMutation } from '@/features/auth/queries'
import { usePendingInvitationCount } from '@/features/notifications/queries'
import { useMeQuery } from '@/features/user/queries'
import { cn } from '@/lib/utils'
import { useServersQuery } from '../queries'
import { JoinServerDialog } from './join-server-dialog'
import { ServerFormDialog } from './server-form-dialog'

type OpenDialog = 'create' | 'join' | null

const railItem =
  'relative flex size-12 cursor-pointer items-center justify-center rounded-3xl bg-background transition-all hover:rounded-xl hover:bg-primary hover:text-primary-foreground'

export function ServerRail() {
  const navigate = useNavigate()
  const [dialog, setDialog] = useState<OpenDialog>(null)
  const { data: servers = [] } = useServersQuery()
  const pendingCount = usePendingInvitationCount()
  const { data: me } = useMeQuery()
  const logout = useLogoutMutation()
  const closeDialog = (open: boolean) => !open && setDialog(null)

  const handleLogout = () =>
    logout.mutate(undefined, { onSuccess: () => navigate({ to: '/login' }) })

  return (
    <nav className='flex w-18 shrink-0 flex-col items-center gap-2 overflow-y-auto bg-muted py-3'>
      <Link
        to='/'
        className={railItem}
        activeOptions={{ exact: true }}
        activeProps={{
          className: 'rounded-xl bg-primary text-primary-foreground'
        }}
      >
        <MessageCircleIcon className='size-5' />
      </Link>
      <Link
        to='/notifications'
        title='Thông báo'
        className={railItem}
        activeProps={{
          className: 'rounded-xl bg-primary text-primary-foreground'
        }}
      >
        <BellIcon className='size-5' />
        {pendingCount > 0 && (
          <span className='absolute -right-1 -bottom-1 rounded-full bg-destructive px-1.5 text-xs text-white'>
            {pendingCount}
          </span>
        )}
      </Link>
      <Separator className='mx-auto w-8' />

      {servers.map((server) => (
        <Link
          key={server.id}
          to='/servers/$serverId'
          params={{ serverId: server.id }}
          title={server.name}
          className={cn(railItem, 'overflow-hidden')}
          activeProps={{ className: 'rounded-xl ring-2 ring-primary' }}
        >
          <UserAvatar
            name={server.name}
            src={server.avatar}
            className='size-full rounded-none'
          />
        </Link>
      ))}

      <button
        type='button'
        title='Tạo server'
        className={cn(railItem, 'text-green-600')}
        onClick={() => setDialog('create')}
      >
        <PlusIcon className='size-5' />
      </button>
      <button
        type='button'
        title='Tham gia bằng link'
        className={railItem}
        onClick={() => setDialog('join')}
      >
        <LinkIcon className='size-5' />
      </button>

      <div className='mt-auto'>
        {me && (
          <DropdownMenu>
            <DropdownMenuTrigger className='cursor-pointer rounded-full'>
              <UserAvatar
                name={me.displayName}
                src={me.avatar}
                className='size-10'
              />
            </DropdownMenuTrigger>
            <DropdownMenuContent side='right' align='end'>
              <div className='px-2 py-1.5 text-sm'>
                <p className='font-medium'>{me.displayName}</p>
                <p className='text-xs text-muted-foreground'>@{me.username}</p>
              </div>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => navigate({ to: '/me' })}>
                <UserIcon /> Hồ sơ
              </DropdownMenuItem>
              <DropdownMenuItem variant='destructive' onClick={handleLogout}>
                <LogOutIcon /> Đăng xuất
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )}
      </div>

      <ServerFormDialog open={dialog === 'create'} onOpenChange={closeDialog} />
      <JoinServerDialog open={dialog === 'join'} onOpenChange={closeDialog} />
    </nav>
  )
}
