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
const syncConfig = syncStore()
const wsUrl = syncConfig.url
const room = syncConfig.room
const token = syncConfig.token
const ydoc = new Y.Doc()
const ytext = ydoc.getText('codemirror')

const wsProvider = new WebsocketProvider(
  wsUrl,
  room,
  ydoc,
  {
    params: {
      token: token, // 作为查询参数添加 token
    }
  },
)

const createIndexedDBProvider = () => {
  indexeddbProvider = new IndexeddbPersistence(syncConfig.room, ydoc)
  return indexeddbProvider
}

const initSync = () => {
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
  
  // select a random color for this user
  const userColor = usercolors[random.uint32() % usercolors.length]
  


  const undoManager = new Y.UndoManager(ytext)
  
  const user = localStorage.getItem('userinfo')!
  const loginUser: LoginStruct = JSON.parse(user)
  
  wsProvider.awareness.setLocalStateField('user', {
    name: loginUser.username,
    color: userColor.color,
    colorLight: userColor.light,
  })
  
  config({
    codeMirrorExtensions(_theme, extensions) {
      return [...extensions, yCollab(ytext, wsProvider.awareness, { undoManager })]
    },
  })
}

export { ytext, wsProvider, indexeddbProvider, createIndexedDBProvider, initSync }