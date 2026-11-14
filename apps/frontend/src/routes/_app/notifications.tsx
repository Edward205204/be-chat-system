import { createFileRoute } from '@tanstack/react-router'
import { NotificationList } from '@/features/notifications/components/notification-list'

export const Route = createFileRoute('/_app/notifications')({
  component: NotificationsPage
})

function NotificationsPage() {
  return (
    <main className='flex-1 overflow-y-auto p-6'>
      <div className='mx-auto grid max-w-2xl gap-4'>
        <h1 className='text-xl font-semibold'>Lời mời vào server</h1>
        <NotificationList />
      </div>
    </main>
  )
}
