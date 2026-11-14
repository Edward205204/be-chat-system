import { UserAvatar } from '@/components/common/user-avatar'
import { formatDate } from '@/lib/date'
import { useImageSrc } from '@/lib/files'
import type { ProfileUser } from '../queries'

/** Hiển thị read-only; dùng được cho cả hồ sơ mình lẫn người khác. */
export function ProfileView({ user }: { user: ProfileUser }) {
  const bannerSrc = useImageSrc(user.banner)
  const isPrivateInfo = 'email' in user // chỉ /users/me mới có email, ngày sinh

  return (
    <div className='overflow-hidden rounded-xl border bg-card'>
      <div className='h-32 bg-muted'>
        {bannerSrc && (
          <img src={bannerSrc} alt='' className='size-full object-cover' />
        )}
      </div>
      <div className='-mt-10 grid gap-3 px-6 pb-6'>
        <UserAvatar
          name={user.displayName}
          src={user.avatar}
          className='size-20 border-4 border-card text-lg'
        />
        <div>
          <h1 className='text-xl font-semibold'>{user.displayName}</h1>
          <p className='text-sm text-muted-foreground'>@{user.username}</p>
        </div>
        <dl className='grid grid-cols-[8rem_1fr] gap-y-1 text-sm'>
          <dt className='text-muted-foreground'>Tham gia</dt>
          <dd>{formatDate(user.createdAt)}</dd>
          {isPrivateInfo && (
            <>
              <dt className='text-muted-foreground'>Email</dt>
              <dd>{user.email}</dd>
              <dt className='text-muted-foreground'>Ngày sinh</dt>
              <dd>{user.dateOfBirth ? formatDate(user.dateOfBirth) : '—'}</dd>
            </>
          )}
        </dl>
      </div>
    </div>
  )
}
