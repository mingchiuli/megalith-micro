/**
 * Blogs store - blog pagination state (pageNum, keywords)
 */
export const blogsStore = defineStore('blogsPageStore', () => {
  const pageNum = ref(1)
  const searchPageNum = ref(1)
  const keywords = ref('')
  return { pageNum, searchPageNum, keywords }
})
