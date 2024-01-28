export interface Visitor {
  dayVisit: number
  weekVisit: number
  monthVisit: number
  yearVisit: number
}

export interface BlogDesc {
  id: number
  title: string
  description: string
  created: string
  link: string
  content?: string
  score?: number
  highlight?: SearchBlogsHighlightStruct,
  value?: SearchBlogsHighlightStruct
}

export interface BlogSys {
  id: number
  title: string
  description: string
  created: string
  updated: string
  content: string
  readCount?: number
  recentReadCount?: number
  status: number
  link: string
}

export interface BlogDelSys {
  id: number
  idx: number
  userId: number
  title: string
  description: string
  content: string
  created: string
  updated: string
  status: number
  link: string
  readCount: number
}

export interface UserSys {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  status: number
  created: string
  lastLogin: string
  role: string
}

export interface MenuSys {
  menuId: number
  parentId: number
  title: string
  component: string
  name: string
  url: string
  type: number
  icon: string
  orderNum: number
  status: number
  children: MenuSys[]
}

export interface RoleSys {
  id: number
  name: string
  code: string
  remark: string
  created: string
  updated: string
  status: number
}

export interface BlogEdit {
  id: number
  title: string
  description: string
  content: string
  link: string
  status: number
  version: number
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

interface SearchBlogsHighlightStruct {
  title: string[]
  description: string[]
  content: string[]
}

export interface SearchFavors {
  id: string
  status: number
  title: string
  description: string
  link: string
  created: string
  updated: string
  score: string
  highlight: SearchFavorsHighlightStruct
}

interface SearchFavorsHighlightStruct {
  title: string[]
  description: string[]
}

export interface LoginStruct {
  username: string
  password: string
}

export interface Token {
  accessToken: string
  refreshToken: string
}

export interface UserInfo {
  nickname: string
  avatar: string
}

export interface Tab {
  title: string
  name: string
}

export interface Menu {
  menuId: number
  component?: string
  url?: string
  icon: string
  orderNum: number
  name: string
  parentId: number
  status: number
  title: string
  type: number
  children: Menu[]
}

export interface JWTStruct {
  role: string
  sub: string
  iat: number
  exp: number
}

export interface CatalogueLabel {
  id: string
  label: string
  dist: number
  children: CatalogueLabel[]
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
  pageNumber: number | undefined
}

export interface Data<T> {
  msg: string
  data: T
}

export enum Status {
  NORMAL = 0,
  BLOCK
}

export enum RoutesEnum {
  CATALOUGE = 0,
  MENU,
  ROUTE
}

export enum OperateTypeCode {
  PARAGRAPH = -2,
  NONE = -1,
  TAIL_APPEND = 0,
  TAIL_SUBTRACT = 1,
  HEAD_APPEND = 2,
  HEAD_SUBTRACT = 3,
  REPLACE = 4,
  REMOVE = 5
}

export enum OperaColor {
  SUCCESS = '#67c23a',
  WARNING = '#e6a23c'
}

export enum FieldName {
  DESCRIPTION = 'description',
  CONTENT = 'content',
  STATUS = 'status',
  LINK = 'link',
  TITLE = 'title'
}

export enum ParaInfo {
  PARA_SPLIT = '\n\n'
}

export enum ParaType {
  NONE,
  TAIL_APPEND,
  TAIL_SUBTRACT
}