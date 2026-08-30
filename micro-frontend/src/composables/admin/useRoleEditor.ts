import type { FormRules } from 'element-plus'
import { computed, reactive } from 'vue'
import { DataPermission, Status } from '@/type/entity'
import { useI18n } from 'vue-i18n'

export type RoleForm = {
  id?: number
  name: string
  code: string
  remark: string
  status: Status
}

export type RoleMenuForm = {
  menuId: number
  title: string
  check: boolean
  children: RoleMenuForm[]
}

export const useRoleEditor = () => {
  const { t } = useI18n()
  const form = reactive<RoleForm>({
    id: undefined,
    name: '',
    code: '',
    remark: '',
    status: Status.NORMAL
  })
  const formRules = computed<FormRules<RoleForm>>(() => ({
    name: [
      {
        required: true,
        message: t('validation.enter', { field: t('admin.name') }),
        trigger: 'blur'
      }
    ],
    code: [
      {
        required: true,
        message: t('validation.enter', { field: t('admin.uniqueCode') }),
        trigger: 'blur'
      }
    ],
    remark: [
      {
        required: true,
        message: t('validation.enter', { field: t('admin.remark') }),
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
  const dataPermissionOptions = computed(() => [
    { value: DataPermission.BLOG_VIEW_ALL, label: t('admin.blogViewAll') },
    { value: DataPermission.BLOG_EDIT_ALL, label: t('admin.blogEditAll') },
    { value: DataPermission.BLOG_DELETE_ALL, label: t('admin.blogDeleteAll') },
    { value: DataPermission.BLOG_EXPORT_ALL, label: t('admin.blogExportAll') }
  ])
  const dataPermissionLabel = (permission: DataPermission) =>
    dataPermissionOptions.value.find((item) => item.value === permission)?.label ?? permission
  const clearForm = () =>
    Object.assign(form, { id: undefined, name: '', code: '', remark: '', status: Status.NORMAL })

  return { form, formRules, dataPermissionOptions, dataPermissionLabel, clearForm }
}
