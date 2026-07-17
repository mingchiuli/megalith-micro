import { config } from 'md-editor-v3'
import { Compartment, type Extension } from '@codemirror/state'
import * as Y from 'yjs'
import { yCollab } from 'y-codemirror.next'
import { WebsocketProvider } from 'y-websocket'
import * as random from 'lib0/random'
import type { UserInfo } from '@/type/entity'
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'
import { logger } from '@/utils/logger'
import { storage } from '@/utils/storage'

const usercolors = [
  { color: '#30bced', light: '#30bced33' },
  { color: '#6eeb83', light: '#6eeb8333' },
  { color: '#ffbc42', light: '#ffbc4233' },
  { color: '#ecd444', light: '#ecd44433' },
  { color: '#ee6352', light: '#ee635233' },
  { color: '#9ac2c9', light: '#9ac2c933' },
  { color: '#8acb88', light: '#8acb8833' },
  { color: '#1be7ff', light: '#1be7ff33' }
]

export const yjsCompartment = new Compartment()
const user = storage.getUserInfo<UserInfo>() || { nickname: 'Anonymous', avatar: '', id: 0 }
let currentProvider: WebsocketProvider | null = null
let currentDoc: Y.Doc | null = null

export const hasYjsDocumentState = (doc: Y.Doc) =>
  Y.decodeStateVector(Y.encodeStateVector(doc)).size > 0

export const shouldInitializeYjsDocument = (
  doc: Y.Doc,
  text: Y.Text,
  initialContent: string
) => initialContent.length > 0 && text.length === 0 && !hasYjsDocumentState(doc)

export const createYjsBindingTransaction = (
  currentDocumentLength: number,
  syncedContent: string,
  extension: Extension
) => ({
  changes: {
    from: 0,
    to: currentDocumentLength,
    insert: syncedContent
  },
  effects: yjsCompartment.reconfigure(extension)
})

export const cleanupYjs = () => {
  if (currentProvider) {
    currentProvider.disconnect()
    currentProvider.destroy()
  }
  if (currentDoc) {
    currentDoc.destroy()
  }
  currentProvider = null
  currentDoc = null
}

export const updateProviderToken = () => {
  if (!currentProvider) return
  const token = storage.getAccessToken()
  if (token) {
    currentProvider.params.token = token
  }
}

export const createYjsExtension = async (roomId: string, initialContent: string) => {
  // 重要说明：
  // 1. 这个方法只在组件挂载时调用一次
  // 2. Yjs 的断线重连是 WebsocketProvider 自动处理的
  // 3. 重连时不会再次调用这个方法
  // 4. 所以不需要"检查是否重用"的逻辑

  // 清理旧连接（如果存在）
  cleanupYjs()

  const userColor = usercolors[random.uint32() % usercolors.length]!

  const ydoc = new Y.Doc()
  const ytext = ydoc.getText()

  const provider = new WebsocketProvider(
      `${API_CONFIG.BASE_WS_URL}${API_ENDPOINTS.COLLABORATION.WS_ROOMS}`,
      roomId,
      ydoc,
      {
        // URL 参数：认证令牌会附加到 WebSocket URL 上
        params: {
          token: storage.getAccessToken() || ''
        },

        // 延迟连接：在配置完成后手动调用 provider.connect()
        connect: false,

        // 定期同步：每3秒向服务器请求一次完整状态，防止增量更新丢失
        resyncInterval: 3000,

        // 重连退避：断线重连时的最大等待时间（采用指数退避策略）
        maxBackoffTime: 10000,

        // 跨标签页通信：启用 BroadcastChannel（同一浏览器的多个标签页可直接通信）
        disableBc: false
      }
  )

  let initialSyncResolved = false
  let resolveInitialSync: (content: string) => void
  const initialSync = new Promise<string>((resolve) => {
    resolveInitialSync = resolve
  })

  provider.on('sync', (isSynced: boolean) => {
    logger.log('Sync event fired, isSynced:', isSynced)
    logger.log('Document length after sync:', ytext.length)

    if (!isSynced || initialSyncResolved) return

    // A state-less document is new or was recreated after its in-memory room expired.
    if (shouldInitializeYjsDocument(ydoc, ytext, initialContent)) {
      logger.log('Inserting initial content:', initialContent.substring(0, 50))
      ytext.insert(0, initialContent)

      ElNotification({
        title: '文档已初始化',
        message: '协同编辑已就绪',
        type: 'success',
        duration: 2000
      })
    }

    initialSyncResolved = true
    resolveInitialSync(ytext.toString())
  })

  // 关键修复6: 监听连接状态
  provider.on('status', (event: { status: 'connected' | 'disconnected' | 'connecting' }) => {
    logger.log('WebSocket status:', event.status)

    switch (event.status) {
      case 'connected':
        logger.log('✅ WebSocket 已连接')
        ElNotification({
          title: '已连接',
          message: '协同编辑连接成功',
          type: 'success',
          duration: 2000
        })
        break

      case 'disconnected':
        logger.warn('⚠️ WebSocket 断开连接')
        // 重要：不要清理 Doc 和 Provider，等待自动重连
        ElNotification({
          title: '连接断开',
          message: '连接已断开',
          type: 'warning',
          duration: 2000
        })
        break

      case 'connecting':
        logger.log('🔄 WebSocket 正在连接...')
        break
    }
  })

  // 关键修复7: 连接错误处理
  provider.on('connection-error', (event: Event) => {
    logger.error('❌ WebSocket 连接错误:', event)
    ElNotification({
      title: '连接错误',
      message: '协同编辑连接失败，将继续重试',
      type: 'error',
      duration: 3000
    })
  })

  // 关键修复8: 连接关闭处理
  provider.on('connection-close', (event: CloseEvent | null) => {
    if (!event) {
      logger.warn('WebSocket 连接关闭: 事件为 null')
      return
    }

    logger.warn('WebSocket 连接关闭:', {
      code: event.code,
      reason: event.reason,
      wasClean: event.wasClean
    })

    // WebSocket 关闭码参考：
    // 1000: 正常关闭
    // 1001: 端点离开（例如：服务器关闭或浏览器导航）
    // 1006: 异常关闭（没有发送关闭帧）
    // 1011: 服务器遇到意外情况

    if (event.code !== 1000) {
      ElNotification({
        title: '连接已断开',
        message: `关闭码: ${event.code}, 原因: ${event.reason || '未知'}`,
        type: 'warning',
        duration: 3000
      })
    }
  })

  const undoManager = new Y.UndoManager(ytext)

  provider.awareness.setLocalStateField('user', {
    name: user.nickname,
    color: userColor.color,
    colorLight: userColor.light
  })

  currentDoc = ydoc
  currentProvider = provider

  const config = yCollab(ytext, provider.awareness, { undoManager })

  return { config, provider, initialSync }
}

config({
  codeMirrorExtensions(extensions) {
    return [
      ...extensions,
      {
        type: 'compartment',
        extension: yjsCompartment.of([])
      }
    ]
  }
})
