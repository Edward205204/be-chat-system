import dayjs from 'dayjs'

/** BE trả LocalDateTime không timezone (giờ VN) → dayjs hiểu là giờ local. */
export function formatMessageTime(value: string): string {
  const d = dayjs(value)
  return d.isSame(dayjs(), 'day')
    ? d.format('HH:mm')
    : d.format('DD/MM/YYYY HH:mm')
}

export const formatDate = (value: string) => dayjs(value).format('DD/MM/YYYY')

export const isFuture = (value: string) => dayjs(value).isAfter(dayjs())

export const minutesBetween = (a: string, b: string) =>
  Math.abs(dayjs(a).diff(dayjs(b), 'minute'))
