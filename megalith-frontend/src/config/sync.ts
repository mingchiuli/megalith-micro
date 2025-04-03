import * as Y from 'yjs'
import * as random from 'lib0/random'
import { yCollab } from 'y-codemirror.next'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import { syncStore } from '@/stores/store'
import { config } from 'md-editor-v3'
import type { LoginStruct } from '@/type/entity'

// 创建 IndexedDB 持久化
let indexeddbProvider: IndexeddbPersistence | null = null
let wsProvider: WebsocketProvider | null = null
let ytext: Y.Text | null = null
let ydoc: Y.Doc | null = null

const createIndexedDBProvider = () => {
  const store = syncStore() // 在函数内部使用
  indexeddbProvider = new IndexeddbPersistence(store.room, ydoc!)
  return indexeddbProvider
}

const initSync = () => {
  // 获取 store 实例
  const store = syncStore()
  
  ydoc = new Y.Doc()
  ytext = ydoc.getText('codemirror')
  
  wsProvider = new WebsocketProvider(
    store.url,
    store.room,
    ydoc,
    {
      params: {
        token: store.token,
      }
    },
  )

  const usercolors = [
    { color: '#30bced', light: '#30bced33' },
    { color: '#6eeb83', light: '#6eeb8333' },
    { color: '#ffbc42', light: '#ffbc4233' },
    { color: '#ecd444', light: '#ecd44433' },
    { color: '#ee6352', light: '#ee635233' },
    { color: '#9ac2c9', light: '#9ac2c933' },
    { color: '#8acb88', light: '#8acb8833' },
    { color: '#1be7ff', light: '#1be7ff33' },
  ]
  
  const userColor = usercolors[random.uint32() % usercolors.length]
  
  const undoManager = new Y.UndoManager(ytext)
  
  // 安全地获取用户信息
  const userStr = localStorage.getItem('userinfo')
  if (userStr) {
    try {
      const loginUser: LoginStruct = JSON.parse(userStr)
      
      wsProvider.awareness.setLocalStateField('user', {
        name: loginUser.username,
        color: userColor.color,
        colorLight: userColor.light,
      })
    } catch (e) {
      console.error('Failed to parse user info', e)
    }
  }
  
  config({
    codeMirrorExtensions(_theme, extensions) {
      return [...extensions, yCollab(ytext!, wsProvider!.awareness, { undoManager })]
    },
  })
}

export { ytext, wsProvider, indexeddbProvider, createIndexedDBProvider, initSync }