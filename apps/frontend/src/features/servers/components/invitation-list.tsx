import { Link } from '@tanstack/react-router'
import { BellIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { usePendingInvitationCount } from '@/features/notifications/queries'

/** Tóm tắt ở trang chủ; danh sách đầy đủ ở /notifications. */
export function InvitationList() {
  const count = usePendingInvitationCount()
  if (count === 0) return null
  return (
    <Button variant='outline' render={<Link to='/notifications' />}>
      <BellIcon /> Bạn có {count} lời mời vào server
    </Button>
  )
}
