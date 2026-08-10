export const displayState = () => {
  const fix = ref<'right' | false>(false)
  const moreItems = ref(false)
  const fixSelection = ref<'left' | false>(false)

  const resize = () => {
    fix.value = document.body.clientWidth > 900 ? 'right' : false
    moreItems.value = document.body.clientWidth > 2000
    fixSelection.value = document.body.clientWidth > 900 ? 'left' : false
  }

  onMounted(() => {
    resize()
    window.addEventListener('resize', resize)
  })
  onUnmounted(() => window.removeEventListener('resize', resize))

  return { fix, moreItems, fixSelection }
}
