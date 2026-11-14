import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { useImageSrc } from '@/lib/files'
import { cn } from '@/lib/utils'

interface UserAvatarProps {
  name: string
  src?: string | null
  className?: string
}

export function UserAvatar({ name, src, className }: UserAvatarProps) {
  const imageSrc = useImageSrc(src)
  return (
    <Avatar className={cn(className)}>
      {imageSrc && <AvatarImage src={imageSrc} alt={name} />}
      <AvatarFallback>{name.slice(0, 2)}</AvatarFallback>
    </Avatar>
  )
}
