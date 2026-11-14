import { useState } from 'react'
import { Trash2Icon, XIcon } from 'lucide-react'
import { LoadMoreList } from '@/components/common/load-more-list'
import { PermissionToggles } from '@/components/common/permission-toggles'
import { UserAvatar } from '@/components/common/user-avatar'
import { Button } from '@/components/ui/button'
import { NativeSelect } from '@/components/ui/native-select'
import { useMembersQuery } from '@/features/servers/queries'
import type { RoleResponse } from '@/types/api'
import { EVERYONE_ROLE, SERVER_PERMISSIONS } from '../constants'
import {
  useDeleteRoleMutation,
  useRoleMemberMutation,
  useRoleMembersQuery,
  useRolePermissionMutation,
  useRolePermissionsQuery,
  useUpdateRoleMutation
} from '../queries'
import { RoleForm } from './role-form'

interface RoleDetailProps {
  serverId: string
  role: RoleResponse
  onDeleted: () => void
}

export function RoleDetail({ serverId, role, onDeleted }: RoleDetailProps) {
  const isEveryone = role.name === EVERYONE_ROLE
  const { data: permissions = [] } = useRolePermissionsQuery(serverId, role.id)
  const togglePermission = useRolePermissionMutation(serverId, role.id)
  const updateRole = useUpdateRoleMutation(serverId, role.id)
  const deleteRole = useDeleteRoleMutation(serverId)

  const handleDelete = () => {
    if (confirm(`Xoá role ${role.name}?`))
      deleteRole.mutate(role.id, { onSuccess: onDeleted })
  }

  return (
    <div className='grid gap-5'>
      {!isEveryone && (
        <section className='grid gap-2'>
          <h3 className='text-sm font-semibold'>Tên và màu</h3>
          <RoleForm
            submitLabel='Lưu'
            defaultValues={{ name: role.name, color: role.color }}
            isPending={updateRole.isPending}
            onSubmit={(values) =>
              updateRole.mutate({
                // Chỉ gửi field đổi: gửi lại tên cũ có thể bị ROLE_NAME_DUPLICATE.
                name: values.name !== role.name ? values.name : undefined,
                color: values.color !== role.color ? values.color : undefined
              })
            }
          />
        </section>
      )}

      <section className='grid gap-2'>
        <h3 className='text-sm font-semibold'>Quyền server</h3>
        <PermissionToggles
          items={SERVER_PERMISSIONS}
          active={permissions}
          disabled={togglePermission.isPending}
          onToggle={(permission, enabled) =>
            togglePermission.mutate({ permission, enabled })
          }
        />
        {isEveryone && (
          <p className='text-xs text-muted-foreground'>
            Áp cho mọi member trong server.
          </p>
        )}
      </section>

      {/* @everyone không có bản ghi gán member nên bỏ qua phần member. */}
      {!isEveryone && <RoleMembers serverId={serverId} roleId={role.id} />}

      {/* BE không chặn xoá @everyone (bug #11) → ẩn nút. */}
      {!isEveryone && (
        <Button
          variant='destructive'
          size='sm'
          className='w-fit'
          onClick={handleDelete}
          disabled={deleteRole.isPending}
        >
          <Trash2Icon /> Xoá role
        </Button>
      )}
    </div>
  )
}

function RoleMembers({
  serverId,
  roleId
}: {
  serverId: string
  roleId: string
}) {
  const { data: roleMembers = [], isPending } = useRoleMembersQuery(
    serverId,
    roleId
  )
  const { data: serverMembers } = useMembersQuery(serverId)
  const mutation = useRoleMemberMutation(serverId, roleId)
  const [selected, setSelected] = useState('')
  const candidates =
    serverMembers?.items.filter(
      (m) => !roleMembers.some((r) => r.memberId === m.memberId)
    ) ?? []

  const handleAdd = () => {
    if (!selected) return
    mutation.mutate(
      { memberId: selected, assign: true },
      { onSuccess: () => setSelected('') }
    )
  }

  return (
    <section className='grid gap-2'>
      <h3 className='text-sm font-semibold'>
        Member có role ({roleMembers.length})
      </h3>
      <div className='flex gap-2'>
        <NativeSelect
          value={selected}
          onChange={(e) => setSelected(e.target.value)}
        >
          <option value=''>Chọn member để thêm…</option>
          {candidates.map((m) => (
            <option key={m.memberId} value={m.memberId}>
              {m.displayName} (@{m.username})
            </option>
          ))}
        </NativeSelect>
        <Button
          variant='secondary'
          onClick={handleAdd}
          disabled={!selected || mutation.isPending}
        >
          Thêm
        </Button>
      </div>
      <LoadMoreList
        height='sm'
        items={roleMembers}
        isPending={isPending}
        emptyTitle='Chưa có member nào'
        getKey={(m) => m.memberId}
        renderItem={(m) => (
          <div className='flex items-center gap-2 px-1'>
            <UserAvatar
              name={m.displayName}
              src={m.avatar}
              className='size-7'
            />
            <span className='flex-1 truncate text-sm'>
              {m.displayName}{' '}
              <span className='text-muted-foreground'>@{m.username}</span>
            </span>
            <Button
              size='icon-sm'
              variant='ghost'
              aria-label='Gỡ khỏi role'
              onClick={() =>
                mutation.mutate({ memberId: m.memberId, assign: false })
              }
            >
              <XIcon />
            </Button>
          </div>
        )}
      />
    </section>
  )
}
