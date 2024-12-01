import { type Ref } from 'vue'

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
  highlight?: SearchBlogsHighlightStruct
  value?: string
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

export interface BlogSysQuery {
  currentPage: number,
  size: number,
  keywords: string,
  createStart: string,
  createEnd: string
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

export interface AuthoritySys {
  id: number
  code: string
  remark: string
  created: string
  updated: string
  status: number
  type: number
  prototype: string
  methodType: string
  routePattern: string
  serviceHost: string
  servicePort: number
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
  updated: string
  lastLogin: string
  roles: string[]
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
  created: string
  updated: string
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
  userId: number
  title: string
  description: string
  content: string
  link: string
  status: number
  version: number
  sensitiveContentList: SensitiveItem[]
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

export interface ChildrenFather {
  [propName: string]: any
  children: any[]
}

export interface Tab {
  title: string
  name: string
}

export interface Button extends Tab {
  [propName: string]: any
  menuId: number
  component?: string
  url?: string
  icon: string
  orderNum: number
  parentId: number
  status: number
  type: number
}

export interface Menu extends ChildrenFather, Button, Tab {}

export interface MenusAndButtons {
  [propName: string]: any
  menus: Menu[]
  buttons: Button[]
}

export interface JWTStruct {
  role: string
  sub: string
  iat: number
  exp: number
}

export interface CatalogueLabel extends ChildrenFather {
  id: string
  label: string
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

export interface SearchPage<T> extends PageAdapter<T> {
  additional: any
}

export interface Data<T> {
  msg: string
  data: T
}

export enum Status {
  NORMAL = 0,
  BLOCK,
  SENSITIVE_FILTER
}

export enum AuthStatus {
  WHITE_LIST = 0,
  NEED_AUTH
}

export enum RoutesEnum {
  CATALOGUE = 0,
  MENU,
  BUTTON
}

export enum OperateTypeCode {
  STATUS = -1,
  NON_PARA_TAIL_APPEND = 0,
  NON_PARA_TAIL_SUBTRACT = 1,
  NON_PARA_HEAD_APPEND = 2,
  NON_PARA_HEAD_SUBTRACT = 3,
  NON_PARA_REPLACE = 4,
  NON_PARA_REMOVE = 5,
  PARA_TAIL_APPEND = 6,
  PARA_TAIL_SUBTRACT = 7,
  PARA_HEAD_APPEND = 8,
  PARA_HEAD_SUBTRACT = 9,
  PARA_REPLACE = 10,
  PARA_REMOVE = 11,
  PARA_SPLIT_APPEND = 12,
  PARA_SPLIT_SUBTRACT = 13,
  SENSITIVE_CONTENT_LIST = 14
}

export enum SubscribeType {
  PUSH_ALL = -1,
  PULL_ALL = -2
}

export enum OperaColor {
  SUCCESS = '#67c23a',
  WARNING = '#e6a23c',
  FAILED = '#FF0000'
}

export enum FieldName {
  DESCRIPTION = 'description',
  CONTENT = 'content',
  STATUS = 'status',
  LINK = 'link',
  TITLE = 'title',
  SENSITIVE_CONTENT_LIST = 'sensitiveContentList'
}

export enum SensitiveType {
  TITLE = 1,
  DESCRIPTION = 2,
  CONTENT = 3
}

export enum FieldType {
  NON_PARA = 'non_para',
  PARA = 'para'
}

export enum ActionType {
  PUSH_ACTION = 'push_action',
  PUSH_ALL = 'push_all',
  PULL_ALL = 'pull_all'
}

export enum ParaInfo {
  PARA_SPLIT = '\n\n'
}

export enum Role {
  ADMIN = 'admin'
}

export enum ButtonAuth {
  SYS_BLOG_DOWNLOAD = 'system-blogs-download',
  SYS_BLOG_SEARCH = 'system-blogs-search',
  SYS_BLOG_BATCH_DEL = 'system-blogs-batch-del',
  SYS_BLOG_DELETE = 'system-blogs-delete',
  SYS_BLOG_CHECK = 'system-blogs-check',
  SYS_BLOG_EDIT = 'system-blogs-edit',
  SYS_BLOG_PASSWORD = 'system-blogs-password',
  SYS_DELETE_RESUME = 'system-delete-resume',
  SYS_EDIT_COMMIT = 'system-edit-submit',

  SYS_AUTHORITY_CREATE = 'system-authorities-create',
  SYS_AUTHORITY_BATCH_DEL = 'system-authorities-batch-del',
  SYS_AUTHORITY_DOWNLOAD = 'system-authorities-download',
  SYS_AUTHORITY_EDIT = 'system-authorities-edit',
  SYS_AUTHORITY_DELETE = 'system-authorities-delete',

  SYS_FAVOR_CREATE = 'system-favors-create',
  SYS_FAVOR_EDIT = 'system-favors-edit',
  SYS_FAVOR_DELETE = 'system-favors-delete',
  SYS_FAVOR_SEARCH = 'system-favors-search',

  SYS_MENU_CREATE = 'system-menus-create',
  SYS_MENU_DELETE = 'system-menus-delete',
  SYS_MENU_EDIT = 'system-menus-edit',
  SYS_MENU_DOWNLOAD = 'system-menus-download',

  SYS_ROLE_CREATE = 'system-roles-create',
  SYS_ROLE_BATCH_DEL = 'system-roles-batch-del',
  SYS_ROLE_DOWNLOAD = 'system-roles-download',
  SYS_ROLE_DELETE = 'system-roles-delete',
  SYS_ROLE_MENU_PERM = 'system-roles-menu-perm',
  SYS_ROLE_AUTHORITY_PERM = 'system-roles-authority-perm',
  SYS_ROLE_EDIT = 'system-roles-edit',

  SYS_USER_CREATE = 'system-users-create',
  SYS_USER_BATCH_DEL = 'system-users-batch-del',
  SYS_USER_REGISTER = 'system-users-register',
  SYS_USER_DELETE = 'system-users-delete',
  SYS_USER_EDIT = 'system-users-edit',
  SYS_USER_MODIFY_REGISTER = 'system-users-modify-register',
  SYS_USER_DOWNLOAD = 'system-users-download'
}

export interface PushActionForm {
  id?: number
  contentChange?: string
  operateTypeCode?: OperateTypeCode
  version?: number
  indexStart?: number
  indexEnd?: number
  field?: FieldName
  paraNo?: number
}

export interface OperateStatusParam {
  composing: boolean
  client: WebSocket
  fieldType: FieldType
  transColor: Ref<string>
  blogId: string | undefined
  readOnly: Ref<boolean>
  pulling: boolean
}

export interface EditForm {
  id?: number
  userId: number | undefined
  title: string | undefined
  description: string | undefined
  content: string | undefined
  status: number | undefined
  link: string | undefined
  version: number
  sensitiveContentList: SensitiveItem[]
}

export interface SensitiveItem {
  startIndex: number
  endIndex: number
  type: SensitiveType
}

export interface SensitiveExhibit {
  startIndex: number
  content: string
  type: SensitiveType
}

export interface SensitiveTrans {
  startIndex: number
  endIndex: number
  type: SensitiveType
}

export interface SubscribeItem {
  version: number
  userId: number
  blogId: number
  type: number
}

export const Colors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 }
]
