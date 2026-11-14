import { useRef, useState, type ChangeEvent } from 'react'
import { ImageIcon, LoaderIcon, UploadIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Field, FieldError, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { getErrorMessage } from '@/lib/errors'
import {
  IMAGE_ACCEPT,
  uploadImage,
  useImageSrc,
  validateImageFile
} from '@/lib/files'
import { cn } from '@/lib/utils'

interface ImageFieldProps {
  label: string
  value: string
  onChange: (value: string) => void
  error?: string
  /** `avatar`: ô vuông bo tròn; `banner`: khung ngang. */
  variant?: 'avatar' | 'banner'
}

/** Ảnh mặc định upload lên BE; nhập URL là tuỳ chọn, ẩn tới khi bấm. Dùng với RHF qua `Controller`. */
export function ImageField({
  label,
  value,
  onChange,
  error,
  variant = 'avatar'
}: ImageFieldProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [isUploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string>()
  const [isUrlOpen, setUrlOpen] = useState(false)
  const previewSrc = useImageSrc(value)

  const handleFile = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = '' // cho phép chọn lại cùng file
    if (!file) return
    const invalid = validateImageFile(file)
    setUploadError(invalid ?? undefined)
    if (invalid) return
    setUploading(true)
    try {
      onChange(await uploadImage(file))
    } catch (err) {
      setUploadError(getErrorMessage(err))
    } finally {
      setUploading(false)
    }
  }

  return (
    <Field>
      <FieldLabel>{label}</FieldLabel>
      <div
        className={cn(
          'flex gap-3',
          variant === 'banner' ? 'flex-col' : 'items-center'
        )}
      >
        <div
          className={cn(
            'flex shrink-0 items-center justify-center overflow-hidden border bg-muted text-muted-foreground',
            variant === 'banner'
              ? 'h-24 w-full rounded-md'
              : 'size-16 rounded-full'
          )}
        >
          {previewSrc ? (
            <img
              src={previewSrc}
              alt={label}
              className='size-full object-cover'
            />
          ) : (
            <ImageIcon className='size-6' />
          )}
        </div>
        <div className='flex flex-wrap items-center gap-2'>
          <Button
            type='button'
            variant='outline'
            size='sm'
            onClick={() => inputRef.current?.click()}
            disabled={isUploading}
          >
            {isUploading ? (
              <LoaderIcon className='animate-spin' />
            ) : (
              <UploadIcon />
            )}
            {value ? 'Đổi ảnh' : 'Tải ảnh lên'}
          </Button>
          {value && (
            <Button
              type='button'
              variant='ghost'
              size='sm'
              onClick={() => onChange('')}
            >
              Bỏ
            </Button>
          )}
          <button
            type='button'
            className='cursor-pointer text-xs text-muted-foreground underline'
            onClick={() => setUrlOpen((o) => !o)}
          >
            {isUrlOpen ? 'Ẩn ô URL' : 'Dùng URL thay thế'}
          </button>
        </div>
      </div>
      <input
        ref={inputRef}
        type='file'
        accept={IMAGE_ACCEPT}
        hidden
        onChange={handleFile}
      />
      {isUrlOpen && (
        <Input
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder='https://...'
        />
      )}
      <FieldError>{uploadError ?? error}</FieldError>
    </Field>
  )
}
