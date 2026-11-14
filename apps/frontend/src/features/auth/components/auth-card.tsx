import type { ReactNode } from 'react'
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card'

interface AuthCardProps {
  title: string
  description?: ReactNode
  footer?: ReactNode
  children: ReactNode
}

export function AuthCard({
  title,
  description,
  footer,
  children
}: AuthCardProps) {
  return (
    <Card className='w-full max-w-sm'>
      <CardHeader>
        <CardTitle className='text-xl'>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardContent>{children}</CardContent>
      {footer && (
        <CardFooter className='justify-center text-sm text-muted-foreground'>
          {footer}
        </CardFooter>
      )}
    </Card>
  )
}
