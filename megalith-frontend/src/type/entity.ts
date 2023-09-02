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

export interface BlogExhibit {
  title: string
  description: string
  created: string
  content: string
  avatar: string
  readCount: number
  nickname: string
}

interface SearchStruct {
  title: string[]
  description: string[]
  content: string[]
}

export interface LoginStruct {
  username: string
  password: string
}

export interface Token {
  accessToken: string
  refreshToken: string
}

export interface JWTStruct {
  role: string
  sub: string
  iat: number
  exp: number
}

export interface CatalogueLabel {
  id: string
  dist: number
}

export interface RefreshStruct {
  accessToken: string
}

export interface Hot {
  id: number
  title: string
  readCount: number
}
export interface PageAdapter<T> {
  content: T[]
  pageSize: number
  totalElements: number
  pageNumber: number
}

export interface Data<T> {
  msg: string
  data: T
}