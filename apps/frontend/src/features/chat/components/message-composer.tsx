import { useRef, useState, type KeyboardEvent } from 'react'
import { MicOffIcon, SendHorizontalIcon } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { useStompStatus } from '@/lib/stomp'
import { sendChatMessage, useMutedSendError } from '../queries'

interface MessageComposerProps {
  serverId: string
  channelId: string
  channelName: string
  isMuted: boolean
}

export function MessageComposer({
  serverId,
  channelId,
  channelName,
  isMuted
}: MessageComposerProps) {
  const [content, setContent] = useState('')
  const isConnected = useStompStatus((s) => s.status === 'connected')
  const lastSentRef = useRef('')

  // BE từ chối vì mute: trả lại nội dung vừa gửi (nếu ô đang trống).
  useMutedSendError(serverId, () =>
    setContent((current) => current || lastSentRef.current)
  )

  const handleSend = () => {
    const text = content.trim()
    if (!text) return
    if (!sendChatMessage(channelId, text))
      return void toast.error('Mất kết nối realtime, đang thử lại…')
    lastSentRef.current = text
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
    <div className='border-t p-3'>
      {isMuted && (
        <p className='mb-2 flex items-center gap-1.5 text-sm text-destructive'>
          <MicOffIcon className='size-4' />
          Bạn đang bị mute trong server này, không thể gửi tin nhắn.
        </p>
      )}
      <div className='flex items-end gap-2'>
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
    </div>
  )
}
