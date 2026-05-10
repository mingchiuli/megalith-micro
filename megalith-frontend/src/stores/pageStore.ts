import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Blogs store - blog pagination state (pageNum, keywords)
 */
export const blogsStore = defineStore('blogsPageStore', () => {
  const pageNum = ref(1)
  const searchPageNum = ref(1)
  const keywords = ref('')
  return { pageNum, searchPageNum, keywords }
})

/**
 * Page store - front/backend page context
 */
export const pageStore = defineStore('pageStore', () => {
  const front = ref(true)
  return { front }
})