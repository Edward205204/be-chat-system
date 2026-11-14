import { createFileRoute, Link, useNavigate } from '@tanstack/react-router'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from '@/components/ui/card'
import { getErrorMessage } from '@/lib/errors'
import { useJoinServerMutation } from '@/features/servers/queries'

export const Route = createFileRoute('/_app/invite/$token')({
  component: InvitePage
})

function InvitePage() {
  const { token } = Route.useParams()
  const navigate = useNavigate()
  const join = useJoinServerMutation()

  const handleJoin = () =>
    join.mutate(token, {
      onSuccess: (res) => {
        toast.success(`Đã vào ${res.serverName}`)
        navigate({
          to: '/servers/$serverId',
          params: { serverId: res.serverId }
        })
      }
    })

  return (
    <main className='flex flex-1 items-center justify-center p-4'>
      <Card className='w-full max-w-sm'>
        <CardHeader>
          <CardTitle>Bạn được mời vào một server</CardTitle>
          <CardDescription>
            {join.isError ? getErrorMessage(join.error) : 'Bấm để tham gia.'}
          </CardDescription>
        </CardHeader>
        <CardContent className='flex gap-2'>
          <Button onClick={handleJoin} disabled={join.isPending}>
            Tham gia
          </Button>
          <Button variant='ghost' render={<Link to='/' />}>
            Bỏ qua
          </Button>
        </CardContent>
      </Card>
    </main>
  )
}
