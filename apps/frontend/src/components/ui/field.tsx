import type { ComponentProps } from 'react'
import { cn } from '@/lib/utils'
import { Label } from '@/components/ui/label'

function Field({ className, ...props }: ComponentProps<'div'>) {
  return (
    <div data-slot='field' className={cn('grid gap-2', className)} {...props} />
  )
}

function FieldLabel(props: ComponentProps<typeof Label>) {
  return <Label data-slot='field-label' {...props} />
}

function FieldError({ className, children, ...props }: ComponentProps<'p'>) {
  if (!children) return null
  return (
    <p
      data-slot='field-error'
      className={cn('text-xs text-destructive', className)}
      {...props}
    >
      {children}
    </p>
  )
}

export { Field, FieldLabel, FieldError }
