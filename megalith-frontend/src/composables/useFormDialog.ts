import type { FormInstance, FormRules } from 'element-plus'
import { POST } from '@/http/http'

/**
 * Options for form dialog composable
 */
export interface FormDialogOptions<T extends object> {
  /**
   * Default form values
   */
  defaultForm: T
  /**
   * Form validation rules
   */
  rules?: FormRules<T>
  /**
   * Save endpoint URL
   */
  saveEndpoint: string
  /**
   * Get item details for editing (optional)
   */
  getFn?: (id: number) => Promise<T>
  /**
   * Callback after successful save
   */
  onSuccess?: () => void
}

/**
 * Composable for form dialog operations
 * Handles dialog visibility, form state, validation, save
 */
export const useFormDialog = <T extends object>(options: FormDialogOptions<T>) => {
  // State
  const dialogVisible = ref(false)
  const formRef = ref<FormInstance>()
  const form = reactive<T>({ ...options.defaultForm } as T)

  /**
   * Open dialog for creating new item
   */
  const openDialog = () => {
    dialogVisible.value = true
  }

  /**
   * Open dialog for editing existing item
   */
  const handleEdit = async (id: number) => {
    if (options.getFn) {
      const data = await options.getFn(id)
      Object.assign(form, data)
    }
    dialogVisible.value = true
  }

  /**
   * Close dialog and clear form
   */
  const handleClose = () => {
    dialogVisible.value = false
    clearForm()
  }

  /**
   * Clear form to default values
   */
  const clearForm = () => {
    Object.assign(form, options.defaultForm)
    // Reset optional fields that may have been set
    for (const key in form) {
      if (!(key in options.defaultForm)) {
        delete (form as Record<string, unknown>)[key]
      }
    }
    formRef.value?.resetFields()
  }

  /**
   * Submit form after validation
   */
  const submitForm = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        await POST<null>(options.saveEndpoint, form as T)
        ElNotification({
          title: '操作成功',
          message: '保存成功',
          type: 'success'
        })
        clearForm()
        dialogVisible.value = false
        options.onSuccess?.()
      }
    })
  }

  return {
    // State
    dialogVisible,
    formRef,
    form,
    // Methods
    openDialog,
    handleEdit,
    handleClose,
    clearForm,
    submitForm
  }
}