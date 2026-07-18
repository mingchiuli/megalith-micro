import { buttonStore } from '@/stores'

export const checkButtonAuth = (name: string) =>
  buttonStore().buttonList.some((item) => item.name === name)

export const getButtonType = (
  name: string
): '' | 'default' | 'success' | 'warning' | 'info' | 'text' | 'primary' | 'danger' =>
  buttonStore().buttonList.find((item) => item.name === name)?.icon as
    '' | 'default' | 'success' | 'warning' | 'info' | 'text' | 'primary' | 'danger'

export const getButtonTitle = (name: string) =>
  buttonStore().buttonList.find((item) => item.name === name)?.title
