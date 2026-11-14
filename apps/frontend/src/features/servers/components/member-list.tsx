import { CrownIcon, EllipsisIcon, MicOffIcon } from 'lucide-react'
import { LoadMoreList } from '@/components/common/load-more-list'
import { UserAvatar } from '@/components/common/user-avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu'
import { useSession } from '@/lib/session'
import type { ServerMemberItem } from '@/types/api'
import { useMemberActionMutation, useMembersQuery, useServer } from '../queries'

export function MemberList({ serverId }: { serverId: string }) {
  const { data, isPending } = useMembersQuery(serverId)
  const { data: server } = useServer(serverId)
  const myUserId = useSession((s) => s.user?.id)

  return (
    <aside className='hidden w-60 shrink-0 flex-col border-l bg-sidebar lg:flex'>
      <div className='flex h-12 items-center border-b px-4 text-sm font-semibold'>
        Thành viên {data && `— ${data.total}`}
      </div>
      <LoadMoreList
        height='fill'
        pageSize={30}
        className='rounded-none border-0'
        items={data?.items ?? []}
        isPending={isPending}
        emptyTitle='Chưa có thành viên'
        getKey={(member) => member.memberId}
        renderItem={(member) => (
          <MemberRow
            serverId={serverId}
            member={member}
            canManage={Boolean(server?.owner) && member.userId !== myUserId}
          />
        )}
      />
    </aside>
  )
}

interface MemberRowProps {
  serverId: string
  member: ServerMemberItem
  canManage: boolean
}

function MemberRow({ serverId, member, canManage }: MemberRowProps) {
  const action = useMemberActionMutation(serverId)
  const confirmThen = (text: string, run: () => void) => confirm(text) && run()

  return (
    <div className='group flex items-center gap-2 rounded-md px-2 py-1.5 hover:bg-accent'>
      <UserAvatar name={member.displayName} src={member.avatar} />
      <div className='min-w-0 flex-1'>
        <p className='flex items-center gap-1 truncate text-sm font-medium'>
          {member.displayName}
          {member.owner && <CrownIcon className='size-3.5 text-amber-500' />}
          {member.muted && <MicOffIcon className='size-3.5 text-destructive' />}
        </p>
        <div className='flex flex-wrap gap-1'>
          {member.roles.map((role) => (
            <span
              key={role.id}
              className='text-xs'
              style={{ color: role.color }}
            >
              {role.name}
            </span>
          ))}
          {member.roles.length === 0 && (
            <span className='truncate text-xs text-muted-foreground'>
              @{member.username}
            </span>
          )}
        </div>
      </div>
      {canManage && !member.owner && (
        <DropdownMenu>
          <DropdownMenuTrigger className='cursor-pointer rounded p-1 opacity-0 group-hover:opacity-100 hover:bg-background'>
            <EllipsisIcon className='size-4' />
          </DropdownMenuTrigger>
          <DropdownMenuContent align='end'>
            <DropdownMenuItem
              onClick={() =>
                action.mutate({
                  type: 'mute',
                  memberId: member.memberId,
                  mute: !member.muted
                })
              }
            >
              {member.muted ? 'Bỏ mute' : 'Mute'}
            </DropdownMenuItem>
            <DropdownMenuItem
              onClick={() =>
                confirmThen(
                  `Chuyển quyền chủ server cho ${member.displayName}?`,
                  () =>
                    action.mutate({
                      type: 'transfer',
                      memberId: member.memberId
                    })
                )
              }
            >
              Chuyển chủ server
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              variant='destructive'
              onClick={() =>
                confirmThen(`Kick ${member.displayName}?`, () =>
                  action.mutate({ type: 'kick', memberId: member.memberId })
                )
              }
            >
              Kick
            </DropdownMenuItem>
            <DropdownMenuItem
              variant='destructive'
              onClick={() =>
                confirmThen(`Ban ${member.displayName}?`, () =>
                  action.mutate({ type: 'ban', userId: member.userId })
                )
              }
            >
              Ban
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      )}
    </div>
  )
}
