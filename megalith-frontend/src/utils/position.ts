import type { TableInstance } from 'element-plus'
import type { ShallowRef } from 'vue'

export const displayState = (tableRef: Readonly<ShallowRef<TableInstance | null>>) => {
  const fix = ref<'right' | false>(false)
  const moreItems = ref(false)
  const fixSelection = ref<'left' | false>(false)

  const resize = () => {
    const width = document.body.clientWidth
    const nextFix = width > 900 ? 'right' : false
    const nextFixSelection = width > 900 ? 'left' : false
    const fixedColumnsChanged = fix.value !== nextFix || fixSelection.value !== nextFixSelection

    fix.value = nextFix
    moreItems.value = width > 2000
    fixSelection.value = nextFixSelection
    if (fixedColumnsChanged) void nextTick(() => tableRef.value?.doLayout())
  }

  onMounted(() => {
    resize()
    window.addEventListener('resize', resize)
  })
  onUnmounted(() => window.removeEventListener('resize', resize))

  return { fix, moreItems, fixSelection }
}
