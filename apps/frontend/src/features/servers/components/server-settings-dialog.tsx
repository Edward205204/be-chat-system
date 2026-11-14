import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle
} from '@/components/ui/dialog'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { RolesTab } from '@/features/roles/components/roles-tab'
import type { ServerResponse } from '@/types/api'
import { BansTab } from './bans-tab'
import { InviteLinksTab } from './invite-links-tab'

interface ServerSettingsDialogProps {
  server: ServerResponse
  open: boolean
  onOpenChange: (open: boolean) => void
}

const TAB_PANEL =
  '-mx-6 min-h-0 flex-1 overflow-y-auto px-6 [scrollbar-gutter:stable]'

export function ServerSettingsDialog({
  server,
  open,
  onOpenChange
}: ServerSettingsDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent size='lg'>
        <DialogHeader>
          <DialogTitle>Cài đặt {server.name}</DialogTitle>
        </DialogHeader>
        <Tabs
          defaultValue='roles'
          className='flex min-h-0 flex-1 flex-col gap-4'
        >
          <TabsList className='shrink-0'>
            <TabsTrigger value='roles'>Role & quyền</TabsTrigger>
            <TabsTrigger value='links'>Link mời</TabsTrigger>
            <TabsTrigger value='bans'>Danh sách ban</TabsTrigger>
          </TabsList>
          <TabsContent value='roles' className={TAB_PANEL}>
            <RolesTab serverId={server.id} />
          </TabsContent>
          <TabsContent value='links' className={TAB_PANEL}>
            <InviteLinksTab serverId={server.id} />
          </TabsContent>
          <TabsContent value='bans' className={TAB_PANEL}>
            <BansTab serverId={server.id} />
          </TabsContent>
        </Tabs>
      </DialogContent>
    </Dialog>
  )
}
