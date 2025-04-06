import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import { yCollab } from 'y-codemirror.next'
import * as random from 'lib0/random'
import { config } from 'md-editor-v3'
import type { LoginStruct } from '@/type/entity'
import { syncStore } from '@/stores/store'

// 创建 Yjs Doc 和文本类型
const ydoc = new Y.Doc()
const ytext = ydoc.getText('codemirror')
const undoManager = new Y.UndoManager(ytext)

let wsProvider: WebsocketProvider | null = null
let indexeddbProvider: IndexeddbPersistence | null = null
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
const userColor = usercolors[random.uint32() % usercolors.length]

// 获取协作扩展，无论协作是否激活都能工作
const getExtension = () => {
  return yCollab(ytext, wsProvider!.awareness, { undoManager })
}

// 更新 CodeMirror 配置，使用当前的 awareness
const updateCodeMirrorConfig = () => {
  // 获取适当的扩展
  const extension = getExtension()

  // 更新 CodeMirror 配置
  config({
    codeMirrorExtensions(_theme, extensions) {
      // 添加新的 yCollab 扩展
      return [...extensions, extension]
    }
  })

  console.log('已更新 CodeMirror 协作配置')
}

// 激活协作功能，连接到服务器
const activate = async (roomId: string) => {
  const store = syncStore()
  store.room = roomId

  if (!store.url || !store.token) {
    console.warn('缺少必要的连接参数')
    return false
  }

  try {
    console.log('正在激活WebSocket协作功能，连接房间:', roomId)

    // 初始化 WebSocket 提供者
    wsProvider = new WebsocketProvider(store.url, roomId, ydoc, {
      params: {
        token: store.token
      }
    })

    // 设置用户信息
    const userStr = localStorage.getItem('userinfo')
    if (userStr) {
      try {
        const loginUser: LoginStruct = JSON.parse(userStr)

        wsProvider.awareness.setLocalStateField('user', {
          name: loginUser.username,
          color: userColor.color,
          colorLight: userColor.light
        })
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
    
    //立即更新 CodeMirror 配置
    updateCodeMirrorConfig()

    // 初始化 IndexedDB 持久化
    indexeddbProvider = new IndexeddbPersistence(roomId, ydoc)
    // 等待IndexedDB同步完成
    await indexeddbProvider.whenSynced
    
    // 等待连接建立
    try {
      await new Promise<void>((resolve, reject) => {
        const timeout = setTimeout(() => {
          reject(new Error('连接超时'))
        }, 5000)

        const onSync = (isSynced: boolean) => {
          if (isSynced) {
            clearTimeout(timeout)
            wsProvider?.off('sync', onSync)
            resolve()
          }
        }

        wsProvider!.on('sync', onSync)

        if (wsProvider!.wsconnected) {
          clearTimeout(timeout)
          resolve()
        }
      })
    } catch (timeoutError) {
      console.warn('等待同步超时，但连接可能仍然有效')
    }

    return true
  } catch (error) {
    console.error('激活协作功能失败:', error)
    deactivate() // 清理任何部分创建的资源
    return false
  }
}

// 停用协作功能，断开连接
const deactivate = () => {
  console.log('停用WebSocket协作功能')

  if (indexeddbProvider) {
    indexeddbProvider.destroy()
    indexeddbProvider = null
  }

  if (wsProvider) {
    wsProvider.disconnect()
    wsProvider = null
  }
}

const clearIndexDbData = () => {
  if (indexeddbProvider) {
    indexeddbProvider.clearData()
  }
}

// 设置文本内容
const setText = (content: string) => {
  ydoc.transact(() => {
    ytext.delete(0, ytext.toString().length)
    ytext.insert(0, content)
  })
}

// 创建单例实例
export { activate, deactivate, setText, ytext, clearIndexDbData }
