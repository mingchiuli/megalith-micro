import type { FormRules } from 'element-plus'
import { computed, reactive } from 'vue'
import { Status } from '@/type/entity'
import { useI18n } from 'vue-i18n'

export type UserForm = {
  id?: number
  username: string
  nickname: string
  password: string
  avatar: string
  email: string
  phone: string
  status: Status
  roles: string[]
}

export const useUserEditor = () => {
  const { t } = useI18n()
  const form = reactive<UserForm>({
    id: undefined,
    username: '',
    nickname: '',
    password: '',
    avatar: '',
    email: '',
    phone: '',
    status: Status.NORMAL,
    roles: []
  })
  const formRules = computed<FormRules<UserForm>>(() => ({
    username: [
      {
        required: true,
        message: t('validation.enter', { field: t('auth.username') }),
        trigger: 'blur'
      }
    ],
    nickname: [
      {
        required: true,
        message: t('validation.enter', { field: t('auth.nickname') }),
        trigger: 'blur'
      }
    ],
    password: [
      {
        required: false,
        message: t('validation.enter', { field: t('auth.password') }),
        trigger: 'blur'
      }
    ],
    avatar: [
      {
        required: true,
        message: t('validation.enter', { field: t('auth.avatarUrl') }),
        trigger: 'blur'
      }
    ],
    email: [
      {
        required: true,
        message: t('validation.enter', { field: t('auth.email') }),
        trigger: 'blur'
      }
    ],
    phone: [
      {
        required: true,
        message: t('validation.enter', { field: t('auth.phone') }),
        trigger: 'blur'
      }
    ],
    roles: [
      {
        required: true,
        message: t('validation.select', { field: t('auth.role') }),
        trigger: 'blur'
      }
    ],
    status: [
      {
        required: true,
        message: t('validation.select', { field: t('common.status') }),
        trigger: 'blur'
      }
    ]
  }))
  const clearForm = () =>
    Object.assign(form, {
      id: undefined,
      username: '',
      nickname: '',
      password: '',
      avatar: '',
      email: '',
      phone: '',
      status: Status.NORMAL,
      roles: []
    })

  return { form, formRules, clearForm }
}
