import type { ReactNode } from 'react'

export function EmptyState({
  title,
  children
}: {
  title: string
  children?: ReactNode
}) {
  return (
    <div className='flex flex-1 flex-col items-center justify-center gap-2 p-8 text-center text-muted-foreground'>
      <p className='font-medium text-foreground'>{title}</p>
      {children}
    </div>
  )
}
