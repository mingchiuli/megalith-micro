/**
 * API URL 统一管理配置文件
 * 包含所有项目中使用的API端点
 */

// 基础配置
export const API_CONFIG = {
  // 主服务器基础URL (从环境变量获取)
  BASE_URL: import.meta.env.VITE_BASE_URL,
  // WebSocket基础URL
  BASE_WS_URL: import.meta.env.VITE_BASE_WS_URL,
  // AI服务器URL
  AI_BASE_URL: 'http://localhost:11434',
  // 请求超时时间
  TIMEOUT: 10000
}

// 认证相关API
export const AUTH_API = {
  // 登录
  LOGIN: '/login',
  // 刷新token
  TOKEN_REFRESH: '/token/refresh',
  // 获取用户信息
  USER_INFO: '/token/userinfo',
  // 获取菜单导航
  MENU_NAV: '/auth/menu/nav',
  // 发送验证码 (sms/email)
  SEND_CODE: (via: string) => `/code/${via}`,
  // 用户注册检查
  REGISTER_CHECK: '/sys/user/register/check',
  // 用户注册保存
  REGISTER_SAVE: '/sys/user/register/save',
  // 注册时图片上传
  REGISTER_IMAGE_UPLOAD: '/sys/user/register/image/upload',
  // 注册时图片删除
  REGISTER_IMAGE_DELETE: '/sys/user/register/image/delete'
}

// 博客相关API - 公开接口
export const BLOG_PUBLIC_API = {
  // 分页获取博客列表
  GET_BLOGS_PAGE: (pageNo: number) => `/public/blog/page/${pageNo}`,
  // 获取博客详情
  GET_BLOG_INFO: (id: number | string) => `/public/blog/info/${id}`,
  // 通过阅读密码获取博客
  GET_SECRET_BLOG: (id: number | string) => `/public/blog/secret/${id}`,
  // 验证阅读密码
  VALIDATE_READ_TOKEN: (id: number) => `/public/blog/token/${id}`,
  // 获取热门博客排行
  GET_HOT_BLOGS: '/public/blog/scores',
  // 获取博客访问统计
  GET_BLOG_STAT: '/public/blog/stat'
}

// 博客相关API - 管理员接口
export const BLOG_ADMIN_API = {
  // 获取博客列表（管理员）
  GET_BLOGS: '/sys/blog/blogs',
  // 删除博客（批量）
  DELETE_BLOGS: '/sys/blog/delete',
  // 获取博客锁定token
  LOCK_BLOG: (id: number) => `/sys/blog/lock/${id}`,
  // 下载博客数据
  DOWNLOAD_BLOGS: '/sys/blog/download',
  // 博客保存/更新
  SAVE_BLOG: '/sys/blog/save',
  // 获取编辑页面数据
  EDIT_PULL_ECHO: '/sys/blog/edit/pull/echo',
  // OSS图片上传
  OSS_UPLOAD: '/sys/blog/oss/upload',
  // OSS图片删除
  OSS_DELETE: '/sys/blog/oss/delete',
  // 获取已删除的博客列表
  GET_DELETED_BLOGS: '/sys/blog/deleted',
  // 恢复已删除的博客
  RECOVER_BLOG: (idx: number) => `/sys/blog/recover/${idx}`
}

// 用户管理API
export const USER_ADMIN_API = {
  // 用户分页查询
  GET_USERS_PAGE: (pageNumber: number) => `/sys/user/page/${pageNumber}`,
  // 获取用户详情
  GET_USER_INFO: (id: number) => `/sys/user/info/${id}`,
  // 保存/更新用户
  SAVE_USER: '/sys/user/save',
  // 删除用户（批量）
  DELETE_USERS: '/sys/user/delete',
  // 下载用户数据
  DOWNLOAD_USERS: '/sys/user/download',
  // 获取注册链接
  GET_REGISTER_LINK: '/sys/user/auth/register/page'
}

// 角色管理API
export const ROLE_ADMIN_API = {
  // 角色分页查询
  GET_ROLES: '/sys/role/roles',
  // 获取角色详情
  GET_ROLE_INFO: (id: number) => `/sys/role/info/${id}`,
  // 保存/更新角色
  SAVE_ROLE: '/sys/role/save',
  // 删除角色（批量）
  DELETE_ROLES: '/sys/role/delete',
  // 下载角色数据
  DOWNLOAD_ROLES: '/sys/role/download',
  // 获取角色菜单权限
  GET_ROLE_MENUS: (id: number) => `/sys/role/menu/${id}`,
  // 设置角色菜单权限
  SET_ROLE_MENUS: (id: number) => `/sys/role/menu/${id}`,
  // 获取所有有效角色
  GET_VALID_ROLES: '/sys/role/valid/all'
}

// 菜单管理API
export const MENU_ADMIN_API = {
  // 获取菜单列表
  GET_MENUS: '/sys/menu/list',
  // 获取菜单详情
  GET_MENU_INFO: (id: number) => `/sys/menu/info/${id}`,
  // 保存/更新菜单
  SAVE_MENU: '/sys/menu/save',
  // 删除菜单
  DELETE_MENU: (id: number) => `/sys/menu/delete/${id}`,
  // 下载菜单数据
  DOWNLOAD_MENUS: '/sys/menu/download',
  // 获取菜单权限
  GET_MENU_AUTHORITY: (id: number) => `/sys/menu/authority/${id}`,
  // 设置菜单权限
  SET_MENU_AUTHORITY: (id: number) => `/sys/menu/authority/${id}`
}

// 权限管理API
export const AUTHORITY_ADMIN_API = {
  // 获取权限列表
  GET_AUTHORITIES: '/sys/authority/list',
  // 获取权限详情
  GET_AUTHORITY_INFO: (id: number) => `/sys/authority/info/${id}`,
  // 保存/更新权限
  SAVE_AUTHORITY: '/sys/authority/save',
  // 删除权限（批量）
  DELETE_AUTHORITIES: '/sys/authority/delete',
  // 下载权限数据
  DOWNLOAD_AUTHORITIES: '/sys/authority/download'
}

// 搜索相关API
export const SEARCH_API = {
  // 搜索博客
  SEARCH_BLOGS: '/search/public/blog'
}

// 实时协作相关API
export const COLLABORATION_API = {
  // WebSocket房间连接
  WS_ROOMS: '/rooms',
  // 检查房间是否存在
  CHECK_ROOM_EXISTS: (roomId: string) => `/rooms/exist/${roomId}`
}

// AI服务相关API
export const AI_API = {
  // 获取AI模型列表
  GET_MODELS: '/api/tags',
  // AI内容生成
  GENERATE_CONTENT: '/api/generate'
}

// API端点分类导出
export const API_ENDPOINTS = {
  AUTH: AUTH_API,
  BLOG_PUBLIC: BLOG_PUBLIC_API,
  BLOG_ADMIN: BLOG_ADMIN_API,
  USER_ADMIN: USER_ADMIN_API,
  ROLE_ADMIN: ROLE_ADMIN_API,
  MENU_ADMIN: MENU_ADMIN_API,
  AUTHORITY_ADMIN: AUTHORITY_ADMIN_API,
  SEARCH: SEARCH_API,
  COLLABORATION: COLLABORATION_API,
  AI: AI_API
}

// 辅助函数：构建查询参数URL
export const buildQueryUrl = (baseUrl: string, params: Record<string, string | number | boolean>): string => {
  const url = new URL(baseUrl, 'http://localhost')
  Object.keys(params).forEach(key => {
    if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
      url.searchParams.append(key, String(params[key]))
    }
  })
  return url.pathname + url.search
}

// 常用的复合URL构建函数
export const buildCommonUrls = {
  // 构建博客查询URL
  blogQuery: (params: {
    currentPage?: number
    size?: number
    keywords?: string
    createStart?: string
    createEnd?: string
    status?: string | number
  }) => {
    return buildQueryUrl(BLOG_ADMIN_API.GET_BLOGS, params)
  },

  // 构建用户查询URL
  userQuery: (pageNumber: number, params: { size?: number } = {}) => {
    return buildQueryUrl(USER_ADMIN_API.GET_USERS_PAGE(pageNumber), params)
  },

  // 构建角色查询URL
  roleQuery: (params: { currentPage?: number; size?: number }) => {
    return buildQueryUrl(ROLE_ADMIN_API.GET_ROLES, params)
  },

  // 构建搜索URL
  searchQuery: (params: {
    keywords?: string
    currentPage?: number
    allInfo?: boolean
  }) => {
    return buildQueryUrl(SEARCH_API.SEARCH_BLOGS, params)
  },

  // 构建博客下载URL
  blogDownload: (params: {
    keywords?: string
    createStart?: string
    createEnd?: string
  }) => {
    return buildQueryUrl(BLOG_ADMIN_API.DOWNLOAD_BLOGS, params)
  }
}

// 默认导出所有配置
export default {
  API_CONFIG,
  API_ENDPOINTS,
  buildQueryUrl,
  buildCommonUrls
}
