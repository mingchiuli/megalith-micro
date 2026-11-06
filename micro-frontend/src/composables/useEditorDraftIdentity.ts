import { onBeforeUnmount, onMounted, ref } from 'vue'
import { logger } from '@/utils/logger'

/** Allocate only in the browser; each editor instance owns its in-flight creation. */
export const useEditorDraftIdentity = (
  blogId: string | undefined,
  create: () => Promise<{ id: number }>,
  navigate: (id: number) => Promise<unknown>
) => {
  const failed = ref(false)
  let pending: Promise<void> | undefined
  let createdId: number | undefined
  let disposed = false
  const createDraft = () => {
    if (blogId || disposed) return Promise.resolve()
    if (pending) return pending
    failed.value = false
    pending = (async () => {
      createdId ??= (await create()).id
      if (!disposed) await navigate(createdId)
    })()
      .catch((error) => {
        if (!disposed) failed.value = true
        logger.warn('Failed to create editor draft:', error)
      })
      .finally(() => {
        pending = undefined
      })
    return pending
  }
  onMounted(() => {
    void createDraft()
  })
  onBeforeUnmount(() => {
    disposed = true
  })
  return { failed, createDraft }
}
