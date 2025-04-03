import * as Y from 'yjs'
import * as random from 'lib0/random'
import { yCollab } from 'y-codemirror.next'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import { syncStore } from '@/stores/store'
import { config } from 'md-editor-v3'
import type { LoginStruct } from '@/type/entity'

// Create a Y Doc instance
const ydoc = new Y.Doc()
// Create a text type for our editor content
const ytext = ydoc.getText('codemirror')

// Initialize WebSocket provider with null and create it in a function
let wsProvider: WebsocketProvider 
let indexeddbProvider: IndexeddbPersistence | null = null

// Initialize synchronization
const initSync = () => {
  const store = syncStore()
  
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
  
  // Safely get user info
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
      return [...extensions, yCollab(ytext, wsProvider.awareness, { undoManager })]
    },
  })
}

// Initialize the sync system immediately
initSync()

// Create IndexedDB provider
const createIndexedDBProvider = () => {
  const store = syncStore()
  indexeddbProvider = new IndexeddbPersistence(store.room, ydoc)
  return indexeddbProvider
}

export { ytext, wsProvider, indexeddbProvider, createIndexedDBProvider, initSync }