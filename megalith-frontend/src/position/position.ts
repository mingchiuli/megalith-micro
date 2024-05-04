import { onMounted, onUnmounted, ref } from "vue"

export const displayState = () => {

  const fix = ref(document.body.clientWidth > 900 ? 'right' : false)
  const expand = ref(document.body.clientWidth > 900)
  const moreItems = ref(document.body.clientWidth > 2000)
  const fixSelection = ref(document.body.clientWidth > 900 ? 'left' : false)

  const resize = () => {
    fix.value = document.body.clientWidth > 900 ? 'right' : false
    expand.value = document.body.clientWidth > 900
    moreItems.value = document.body.clientWidth > 2000
    fixSelection.value = document.body.clientWidth > 900 ? 'left' : false
  }

  onMounted(() => window.addEventListener('resize', resize))
  onUnmounted(() => window.removeEventListener('resize', resize))

  return { fix, expand, moreItems, fixSelection }
}