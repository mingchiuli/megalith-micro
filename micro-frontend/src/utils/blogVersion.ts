import type { BlogVersionSaveResponse, EditForm } from '@/type/entity'
import { API_ENDPOINTS } from '@/config/apiConfig'

type Post = <T>(url: string, body: unknown) => Promise<T>

export const saveBlogVersion = (post: Post, blogId: number, form: EditForm) =>
  post<BlogVersionSaveResponse>(API_ENDPOINTS.BLOG_ADMIN.SAVE_VERSION(blogId), form)
