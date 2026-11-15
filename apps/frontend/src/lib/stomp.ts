import { Client, type IMessage } from '@stomp/stompjs'
import { create } from 'zustand'
import { toast } from 'sonner'
import { STOMP, type StompErrorPayload } from '@/types/api'
import { isTokenExpired } from '@/lib/jwt'
import { refreshAccessToken } from '@/lib/http'
import { ApiError, getErrorMessage } from '@/lib/errors'
import { useSession } from '@/lib/session'

type StompStatus = 'idle' | 'connecting' | 'connected'

/** Đổi `status` (và `connectionId` mỗi lần connect) để hook subscribe lại sau reconnect. */
export const useStompStatus = create<{
  status: StompStatus
  connectionId: number
}>(() => ({
  status: 'idle',
  connectionId: 0
}))

const setStatus = (status: StompStatus) =>
  useStompStatus.setState((s) => ({
    status,
    connectionId: status === 'connected' ? s.connectionId + 1 : s.connectionId
  }))

let client: Client | null = null

type StompErrorListener = (error: ApiError) => void
const errorListeners = new Set<StompErrorListener>()

/** Nghe lỗi `/user/queue/errors` (lỗi của `chat.send`). Toast chung vẫn hiện ở đây. */
export function addStompErrorListener(listener: StompErrorListener) {
  errorListeners.add(listener)
  return () => void errorListeners.delete(listener)
}

const wsUrl = () =>
  `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}${STOMP.endpoint}`

export function connectStomp() {
  if (client?.active) return
  client = new Client({
    brokerURL: wsUrl(),
    reconnectDelay: 3000,
    // BE chỉ kiểm token lúc CONNECT → luôn lấy token còn hạn trước mỗi lần (re)connect.
    beforeConnect: async (c) => {
      setStatus('connecting')
      let token = useSession.getState().accessToken
      if (!token) return void c.deactivate()
      if (isTokenExpired(token)) token = await refreshAccessToken()
      c.connectHeaders = { Authorization: `Bearer ${token}` }
    },
    onConnect: () => {
      setStatus('connected')
      client?.subscribe(STOMP.errors, (frame) => {
        const payload = JSON.parse(frame.body) as StompErrorPayload
        const error = new ApiError(payload.status, payload.message)
        toast.error(getErrorMessage(error))
        errorListeners.forEach((listener) => listener(error))
      })
    },
    onWebSocketClose: () => {
      if (client?.active) setStatus('connecting')
    },
    onStompError: (frame) =>
      console.error('STOMP error', frame.headers.message, frame.body)
  })
  client.activate()
}

export async function disconnectStomp() {
  const current = client
  client = null
  setStatus('idle')
  await current?.deactivate()
}

export function subscribeJson<T>(
  destination: string,
  onMessage: (payload: T) => void
): () => void {
  if (!client?.connected) return () => {}
  const sub = client.subscribe(destination, (frame: IMessage) =>
    onMessage(JSON.parse(frame.body) as T)
  )
  return () => {
    if (client?.connected) sub.unsubscribe()
  }
}

export function publishJson(destination: string, body: unknown): boolean {
  if (!client?.connected) return false
  client.publish({ destination, body: JSON.stringify(body) })
  return true
}
