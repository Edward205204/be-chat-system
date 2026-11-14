import { createFileRoute } from '@tanstack/react-router'
import { ProfilePage } from '@/features/user/components/profile-page'

export const Route = createFileRoute('/_app/users/$userId')({
  component: UserProfileRoute
})

function UserProfileRoute() {
  const { userId } = Route.useParams()
  return (
    <main className='flex-1 overflow-y-auto p-6'>
      <div className='mx-auto max-w-2xl'>
        <ProfilePage key={userId} userId={userId} />
      </div>
    </main>
  )
}
