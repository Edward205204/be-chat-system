import { useQuery } from '@tanstack/react-query'
import { API_BASE, http, request } from '@/lib/http'

/** Giới hạn mặc định của Spring (1 MB/file); vượt quá BE trả 500. */
export const MAX_UPLOAD_BYTES = 1024 * 1024
export const IMAGE_ACCEPT = 'image/png,image/jpeg,image/webp,image/gif'

const OWN_FILE_PREFIX = `${API_BASE}/files/`

/**
 * Upload ảnh và trả URL tuyệt đối qua proxy (vd http://localhost:3000/rchat/files/x.png).
 * BE trả path tương đối `/files/x`, nhưng field avatar/banner bị validate `@URL` → phải tuyệt đối.
 */
export async function uploadImage(file: File): Promise<string> {
  const form = new FormData()
  form.append('file', file)
  const path = await request<string>({
    method: 'POST',
    url: '/files/upload',
    data: form
  })
  return `${window.location.origin}${API_BASE}${path}`
}

export function validateImageFile(file: File): string | null {
  if (!IMAGE_ACCEPT.split(',').includes(file.type))
    return 'Chỉ nhận ảnh PNG, JPG, WEBP, GIF'
  if (file.size > MAX_UPLOAD_BYTES) return 'Ảnh tối đa 1 MB'
  return null
}

const isOwnFile = (src: string) => {
  try {
    return new URL(src, window.location.origin).pathname.startsWith(
      OWN_FILE_PREFIX
    )
  } catch {
    return false
  }
}

/**
 * `GET /files/{name}` cần token nên `<img src>` bị 401 → tải blob bằng axios rồi tạo object URL.
 * URL ảnh ngoài thì trả nguyên.
 */
export function useImageSrc(
  src: string | null | undefined
): string | undefined {
  const own = Boolean(src) && isOwnFile(src!)
  const { data } = useQuery({
    queryKey: ['file-blob', src],
    queryFn: async () => {
      const path = new URL(src!, window.location.origin).pathname.slice(
        API_BASE.length
      )
      const res = await http.get<Blob>(path, { responseType: 'blob' })
      return URL.createObjectURL(res.data)
    },
    enabled: own,
    staleTime: Infinity,
    gcTime: Infinity,
    retry: false
  })
  if (!src) return undefined
  return own ? data : src
}
