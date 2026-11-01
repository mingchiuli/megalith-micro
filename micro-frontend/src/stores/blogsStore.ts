/**
 * Blogs store - blog search state (searchPageNum, keywords)
 */
export const blogsStore = defineStore('blogsPageStore', () => {
  const searchPageNum = ref(1)
  const keywords = ref('')
  return { searchPageNum, keywords }
})
