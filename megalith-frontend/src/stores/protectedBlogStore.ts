import type { BlogExhibit } from '@/type/entity'

export const protectedBlogStore = defineStore('protectedBlogStore', () => {
  const entries = ref<Record<string, BlogExhibit>>({})

  const put = (blogId: number | string, blog: BlogExhibit) => {
    entries.value[String(blogId)] = blog
  }

  const take = (blogId: number | string): BlogExhibit | undefined => {
    const key = String(blogId)
    const blog = entries.value[key]
    delete entries.value[key]
    return blog
  }

  const clear = () => {
    entries.value = {}
  }

  return { entries, put, take, clear }
})
