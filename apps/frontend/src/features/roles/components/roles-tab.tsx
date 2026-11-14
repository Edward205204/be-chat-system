import { useState } from 'react'
import { EmptyState } from '@/components/common/empty-state'
import { LoadMoreList } from '@/components/common/load-more-list'
import { cn } from '@/lib/utils'
import { useCreateRoleMutation, useRolesQuery } from '../queries'
import { RoleDetail } from './role-detail'
import { RoleForm } from './role-form'

/** Danh sách role bên trái, chi tiết role đang chọn bên phải. */
export function RolesTab({ serverId }: { serverId: string }) {
  const { data: roles = [], isPending } = useRolesQuery(serverId)
  const createRole = useCreateRoleMutation(serverId)
  const [selectedId, setSelectedId] = useState<string>()
  const selected = roles.find((r) => r.id === selectedId) ?? roles[0]

  return (
    <div className='grid gap-4'>
      <RoleForm
        submitLabel='Tạo role'
        isPending={createRole.isPending}
        onSubmit={(values, reset) =>
          createRole.mutate(values, {
            onSuccess: (role) => {
              reset()
              setSelectedId(role.id)
            }
          })
        }
      />
      <div className='grid items-start gap-4 sm:grid-cols-[11rem_1fr]'>
        <LoadMoreList
          height='lg'
          items={roles}
          isPending={isPending}
          emptyTitle='Chưa có role'
          getKey={(role) => role.id}
          renderItem={(role) => (
            <button
              type='button'
              onClick={() => setSelectedId(role.id)}
              className={cn(
                'flex w-full cursor-pointer items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm hover:bg-accent',
                role.id === selected?.id && 'bg-accent font-medium'
              )}
            >
              <span
                className='size-3 shrink-0 rounded-full'
                style={{ backgroundColor: role.color }}
              />
              <span className='truncate'>{role.name}</span>
            </button>
          )}
        />
        {selected ? (
          <RoleDetail
            key={selected.id}
            serverId={serverId}
            role={selected}
            onDeleted={() => setSelectedId(undefined)}
          />
        ) : (
          <EmptyState title='Chưa có role' />
        )}
      </div>
    </div>
  )
}
