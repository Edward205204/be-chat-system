import { cn } from '@/lib/utils'

interface PermissionToggleItem<K extends string> {
  key: K
  label: string
}

interface PermissionTogglesProps<K extends string> {
  items: readonly PermissionToggleItem<K>[]
  active: readonly K[]
  onToggle: (key: K, enabled: boolean) => void
  disabled?: boolean
}

/** Dãy chip bật/tắt quyền. Bật → gọi POST, tắt → DELETE (PUT không cho mảng rỗng). */
export function PermissionToggles<K extends string>({
  items,
  active,
  onToggle,
  disabled
}: PermissionTogglesProps<K>) {
  return (
    <div className='flex flex-wrap gap-1.5'>
      {items.map(({ key, label }) => {
        const isOn = active.includes(key)
        return (
          <button
            key={key}
            type='button'
            title={key}
            disabled={disabled}
            aria-pressed={isOn}
            onClick={() => onToggle(key, !isOn)}
            className={cn(
              'cursor-pointer rounded-md border px-2 py-1 text-xs transition-colors disabled:opacity-50',
              isOn
                ? 'border-primary bg-primary text-primary-foreground'
                : 'hover:bg-accent'
            )}
          >
            {label}
          </button>
        )
      })}
    </div>
  )
}
