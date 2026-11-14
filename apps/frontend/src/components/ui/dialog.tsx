import type { ComponentProps } from 'react'
import { Dialog as DialogPrimitive } from '@base-ui/react/dialog'
import { XIcon } from 'lucide-react'
import { cn } from '@/lib/utils'

const Dialog = DialogPrimitive.Root
const DialogTrigger = DialogPrimitive.Trigger
const DialogClose = DialogPrimitive.Close

/**
 * Kích thước chuẩn của popup. Neo mép trên (không căn giữa dọc) để nội dung đổi
 * chiều cao không làm popup nhảy. `lg` có chiều cao cố định, nội dung cuộn trong `DialogBody`.
 */
const DIALOG_SIZE = {
  sm: 'sm:max-w-md max-h-[80svh]',
  lg: 'flex h-[min(40rem,80svh)] flex-col sm:max-w-2xl'
} as const

function DialogContent({
  className,
  children,
  size = 'sm',
  ...props
}: DialogPrimitive.Popup.Props & { size?: keyof typeof DIALOG_SIZE }) {
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Backdrop className='fixed inset-0 z-50 bg-black/50 transition-opacity data-ending-style:opacity-0 data-starting-style:opacity-0' />
      <DialogPrimitive.Popup
        data-slot='dialog-content'
        className={cn(
          'fixed top-[10svh] left-1/2 z-50 grid w-full max-w-[calc(100%-2rem)] -translate-x-1/2 gap-4 overflow-y-auto rounded-lg border bg-background p-6 shadow-lg transition-[opacity,scale] outline-none [scrollbar-gutter:stable] data-ending-style:scale-95 data-ending-style:opacity-0 data-starting-style:scale-95 data-starting-style:opacity-0',
          DIALOG_SIZE[size],
          size === 'lg' && 'overflow-hidden',
          className
        )}
        {...props}
      >
        {children}
        <DialogPrimitive.Close className='absolute top-4 right-4 cursor-pointer rounded-xs opacity-70 hover:opacity-100'>
          <XIcon className='size-4' />
          <span className='sr-only'>Đóng</span>
        </DialogPrimitive.Close>
      </DialogPrimitive.Popup>
    </DialogPrimitive.Portal>
  )
}

function DialogHeader({ className, ...props }: ComponentProps<'div'>) {
  return <div className={cn('flex flex-col gap-2', className)} {...props} />
}

/** Vùng cuộn của popup `lg`: header đứng yên, chỉ phần này cuộn. */
function DialogBody({ className, ...props }: ComponentProps<'div'>) {
  return (
    <div
      data-slot='dialog-body'
      className={cn(
        '-mx-6 min-h-0 flex-1 overflow-y-auto px-6 [scrollbar-gutter:stable]',
        className
      )}
      {...props}
    />
  )
}

function DialogFooter({ className, ...props }: ComponentProps<'div'>) {
  return (
    <div
      className={cn(
        'flex flex-col-reverse gap-2 sm:flex-row sm:justify-end',
        className
      )}
      {...props}
    />
  )
}

function DialogTitle({ className, ...props }: DialogPrimitive.Title.Props) {
  return (
    <DialogPrimitive.Title
      className={cn('text-lg leading-none font-semibold', className)}
      {...props}
    />
  )
}

function DialogDescription({
  className,
  ...props
}: DialogPrimitive.Description.Props) {
  return (
    <DialogPrimitive.Description
      className={cn('text-sm text-muted-foreground', className)}
      {...props}
    />
  )
}

export {
  Dialog,
  DialogTrigger,
  DialogClose,
  DialogContent,
  DialogHeader,
  DialogBody,
  DialogFooter,
  DialogTitle,
  DialogDescription
}
