import { useState } from 'react'
import { PencilIcon } from 'lucide-react'
import { EmptyState } from '@/components/common/empty-state'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import type { UserResponse } from '@/types/api'
import { useUser } from '../queries'
import { ProfileEditForm } from './profile-edit-form'
import { ProfileView } from './profile-view'

export function ProfilePage({ userId }: { userId: string }) {
  const { user, isPending, isOwner, isSupported } = useUser(userId)
  const [isEditing, setEditing] = useState(false)

  if (!isSupported) {
    return (
      <EmptyState title='Chưa hỗ trợ xem hồ sơ người khác'>
        Tính năng sẽ mở khi BE sẵn sàng.
      </EmptyState>
    )
  }
  if (isPending) return <Skeleton className='h-72' />
  if (!user) return <EmptyState title='Không tìm thấy người dùng' />

  return (
    <div className='grid gap-4'>
      <ProfileView user={user} />
      {isOwner &&
        (isEditing ? (
          <ProfileEditForm
            user={user as UserResponse}
            onDone={() => setEditing(false)}
          />
        ) : (
          <Button
            variant='outline'
            className='w-fit'
            onClick={() => setEditing(true)}
          >
            <PencilIcon /> Sửa hồ sơ
          </Button>
        ))}
    </div>
  )
}
