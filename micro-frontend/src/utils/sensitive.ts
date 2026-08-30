import type { TagProps } from 'element-plus'
import type { EditForm } from '@/type/entity'
import { SensitiveType } from '@/type/entity'

export const getSensitiveTagType = (type: SensitiveType): TagProps['type'] => {
  if (type === SensitiveType.TITLE) return 'success'
  if (type === SensitiveType.DESCRIPTION) return 'primary'
  return 'warning'
}

export const getSensitiveText = (type: SensitiveType, form: EditForm): string => {
  if (type === SensitiveType.TITLE) return form.title ?? ''
  if (type === SensitiveType.DESCRIPTION) return form.description ?? ''
  return form.content ?? ''
}
