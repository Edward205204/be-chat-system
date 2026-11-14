import { useState, type KeyboardEvent } from 'react'
import { SendHorizontalIcon } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { useStompStatus } from '@/lib/stomp'
import { sendChatMessage } from '../queries'

interface MessageComposerProps {
  channelId: string
  channelName: string
  /** BE chưa chặn member bị mute (bug #17) → chặn ở FE. */
  isMuted: boolean
}

export function MessageComposer({
  channelId,
  channelName,
  isMuted
}: MessageComposerProps) {
  const [content, setContent] = useState('')
  const isConnected = useStompStatus((s) => s.status === 'connected')

  const handleSend = () => {
    const text = content.trim()
    if (!text) return
    if (!sendChatMessage(channelId, text))
      return void toast.error('Mất kết nối realtime, đang thử lại…')
    setContent('')
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey && !e.nativeEvent.isComposing) {
      e.preventDefault()
      handleSend()
    }
  }

  const placeholder = isMuted
    ? 'Bạn đang bị mute trong server này'
    : isConnected
      ? `Nhắn #${channelName} (Enter gửi, Shift+Enter xuống dòng)`
      : 'Đang kết nối…'

  return (
    <div className='flex items-end gap-2 border-t p-3'>
      <Textarea
        rows={1}
        className='max-h-40'
        value={content}
        onChange={(e) => setContent(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={isMuted}
      />
      <Button
        size='icon'
        onClick={handleSend}
        disabled={isMuted || !isConnected || !content.trim()}
        aria-label='Gửi'
      >
        <SendHorizontalIcon />
      </Button>
    </div>
  )
}
