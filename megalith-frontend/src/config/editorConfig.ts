import { config } from 'md-editor-v3'
import { Compartment, type Extension } from '@codemirror/state'
import * as Y from 'yjs'
import { yCollab } from 'y-codemirror.next'
import { WebsocketProvider } from 'y-websocket'
import * as random from 'lib0/random'
import type { UserInfo } from '@/type/entity'

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
const userStr = localStorage.getItem('userinfo')!
const user: UserInfo = JSON.parse(userStr)
let currentProvider: WebsocketProvider | null = null
let currentDoc: Y.Doc | null = null

export const cleanupYjs = () => {
  currentProvider?.destroy()
  currentDoc?.destroy()
  currentProvider = null
  currentDoc = null
}

export const updateProviderToken = () => {
  if (!currentProvider) return
  currentProvider.params.token = localStorage.getItem('accessToken')!
}

export const createYjsExtension = (roomId: string, initialContent: string | undefined): Extension => {
  cleanupYjs()

  const userColor = usercolors[random.uint32() % usercolors.length]

  const ydoc = new Y.Doc()
  const provider = new WebsocketProvider(
    `${import.meta.env.VITE_BASE_WS_URL}/rooms`,
    roomId,
    ydoc,
    {
      params: {
        token: localStorage.getItem('accessToken')!
      },
      connect: true,
      resyncInterval: 3000,
      maxBackoffTime: 10000
    }
  )
  const ytext = ydoc.getText()
  
  provider.on('sync', () => {
  
      // 同步完成后检查yText是否为空
      const syncedYtextContent = ytext.toString();
      // 条件：同步后yText为空 + 存在初始内容 → 说明是第一个进入房间的用户
      if (syncedYtextContent === '' && initialContent) {
        Y.transact(ydoc, () => {
          ytext.insert(0, initialContent); // 注入初始内容
        });
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

  return yCollab(ytext, provider.awareness, { undoManager })
}

config({
  codeMirrorExtensions(_theme, extensions) {
    return [...extensions, yjsCompartment.of([])]
  }
})
