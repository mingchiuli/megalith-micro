import { config } from 'md-editor-v3'
import { Compartment } from '@codemirror/state'
import * as Y from 'yjs'
import { yCollab } from 'y-codemirror.next'
import { WebsocketProvider } from 'y-websocket'
import * as random from 'lib0/random'
import type { CheckRoom, UserInfo } from '@/type/entity'
import { GET } from '@/http/http'
import { API_CONFIG, API_ENDPOINTS } from '@/config/apiConfig'

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

export const createYjsExtension = async (
  roomId: string,
  initialContent: string
) => {
  cleanupYjs()

  const userColor = usercolors[random.uint32() % usercolors.length]!

  const ydoc = new Y.Doc()
  const provider = new WebsocketProvider(
    `${API_CONFIG.BASE_WS_URL}${API_ENDPOINTS.COLLABORATION.WS_ROOMS}`,
    roomId,
    ydoc,
    {
      params: {
        token: localStorage.getItem('accessToken')!
      },
      connect: false,
      resyncInterval: 3000,
      maxBackoffTime: 10000
    }
  )

  const ytext = ydoc.getText()
  // 等同步完成后判断是否插入内容
  provider.once('sync', async () => {
    const data = await GET<CheckRoom>(API_ENDPOINTS.COLLABORATION.CHECK_ROOM_EXISTS(roomId))
    if (!data.exists) {
      ytext.insert(0, initialContent)
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
  
  return {
    config, provider
  }
}

config({
  codeMirrorExtensions(extensions) {
    // 1. 先修改 linkShortener 扩展的配置（关键：必须先处理原扩展，再添加自定义扩展）
    const modifiedExtensions = extensions.map((item) => {
      if (item.type === 'linkShortener') {
        return {
          ...item,
          options: {
            maxLength: 10e9, // 设大值相当于禁用缩短
          },
        };
      }
      return item;
    });

    // 2. 再添加 Yjs 的 compartment 扩展（顺序：原扩展处理完后，再追加自定义扩展）
    return [
      ...modifiedExtensions, // 使用修改后的扩展数组
      {
        type: 'compartment',
        extension: yjsCompartment.of([])
      }
    ];
  }
});
