export interface Visitor {
  dayVisit: number
  weekVisit: number
  monthVisit: number
  yearVisit: number
}

export interface BlogsDesc {
  id: number
  title: string
  description: string
  created: string
  link: string
  content?: string
  score?: number
  highlight?: SearchStruct,
  value?: SearchStruct
}

interface SearchStruct {
  title: string[]
  description: string[]
  content: string[]  
}

export interface PageAdapter<T> {
  content: T[]
  pageSize: number
  totalElements: number
  pageNumber: number
}

export interface Data<T> {
  status: number
  msg: string
  data: T
}