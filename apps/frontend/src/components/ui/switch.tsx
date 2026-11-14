import { Switch as SwitchPrimitive } from '@base-ui/react/switch'
import { cn } from '@/lib/utils'

function Switch({ className, ...props }: SwitchPrimitive.Root.Props) {
  return (
    <SwitchPrimitive.Root
      data-slot='switch'
      className={cn(
        'inline-flex h-5 w-9 shrink-0 cursor-pointer items-center rounded-full border border-transparent bg-input shadow-xs transition-colors outline-none data-checked:bg-primary',
        className
      )}
      {...props}
    >
      <SwitchPrimitive.Thumb className='pointer-events-none block size-4 rounded-full bg-background shadow transition-transform data-checked:translate-x-4' />
    </SwitchPrimitive.Root>
  )
}

export { Switch }
