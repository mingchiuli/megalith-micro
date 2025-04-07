import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { IndexeddbPersistence } from 'y-indexeddb'
import { yCollab } from 'y-codemirror.next'
import * as random from 'lib0/random'
import { config } from 'md-editor-v3'
import type { LoginStruct } from '@/type/entity'
import { syncStore } from '@/stores/store'

export class CollaborationManager {
  // 私有属性
  private ydoc: Y.Doc
  private ytext: Y.Text
  private undoManager: Y.UndoManager
  private wsProvider: WebsocketProvider | null = null
  private indexeddbProvider: IndexeddbPersistence | null = null
  private userColor: { color: string; light: string }

  constructor() {
    // 创建新的 Y.Doc 和文本
    this.ydoc = new Y.Doc()
    this.ytext = this.ydoc.getText('codemirror')
    this.undoManager = new Y.UndoManager(this.ytext)

    // 设置用户颜色
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
    this.userColor = usercolors[random.uint32() % usercolors.length]

    console.log('已创建新的协作管理器实例')
  }

  // 获取y-text实例，用于直接读取内容
  public getYText(): Y.Text {
    return this.ytext
  }

  // 更新 CodeMirror 配置
  private updateCodeMirrorConfig() {
    if (!this.wsProvider) {
      console.warn('无法更新CodeMirror配置：WebSocket提供程序未初始化')
      return
    }

    const extension = yCollab(this.ytext, this.wsProvider.awareness, {
      undoManager: this.undoManager
    })

    config({
      codeMirrorExtensions(_theme, extensions) {
        return [...extensions, extension]
      }
    })

    console.log('已更新CodeMirror协作配置')
  }

  // 激活协作功能
  public async activate(roomId: string): Promise<boolean> {
    const store = syncStore()
    store.room = roomId

    if (!store.url || !store.token) {
      console.warn('缺少必要的连接参数')
      return false
    }

    try {
      console.log('正在激活WebSocket协作功能，连接房间:', roomId)

      // 初始化WebSocket提供程序
      this.wsProvider = new WebsocketProvider(store.url, roomId, this.ydoc, {
        params: {
          token: store.token
        }
      })

      // 设置用户信息
      const userStr = localStorage.getItem('userinfo')
      if (userStr) {
        try {
          const loginUser: LoginStruct = JSON.parse(userStr)

          this.wsProvider.awareness.setLocalStateField('user', {
            name: loginUser.username,
            color: this.userColor.color,
            colorLight: this.userColor.light
          })
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }

      // 更新CodeMirror配置
      this.updateCodeMirrorConfig()

      // 初始化IndexedDB持久化
      this.indexeddbProvider = new IndexeddbPersistence(roomId, this.ydoc)

      // 等待IndexedDB同步完成
      await this.indexeddbProvider.whenSynced
      console.log('IndexedDB同步完成')

      // 等待连接建立
      try {
        await new Promise<void>((resolve, reject) => {
          const timeout = setTimeout(() => {
            reject(new Error('连接超时'))
          }, 5000)

          const onSync = (isSynced: boolean) => {
            if (isSynced) {
              clearTimeout(timeout)
              this.wsProvider?.off('sync', onSync)
              resolve()
            }
          }

          this.wsProvider!.on('sync', onSync)

          if (this.wsProvider!.wsconnected) {
            clearTimeout(timeout)
            resolve()
          }
        })
        console.log('WebSocket同步完成')
      } catch (timeoutError) {
        console.warn('等待同步超时，但连接可能仍然有效')
      }

      return true
    } catch (error) {
      console.error('激活协作功能失败:', error)
      this.deactivate() // 清理资源
      return false
    }
  }

  // 停用协作功能
  public deactivate(): void {
    console.log('停用WebSocket协作功能')

    if (this.indexeddbProvider) {
      this.indexeddbProvider.destroy()
      this.indexeddbProvider = null
    }

    if (this.wsProvider) {
      this.wsProvider.disconnect()
      this.wsProvider = null
    }
  }

  // 清除IndexedDB数据
  public clearIndexDbData(): void {
    if (this.indexeddbProvider) {
      this.indexeddbProvider.clearData()
      console.log('已清理IndexedDB数据')
    }
  }

  // 设置文本内容
  public setText(content: string): void {
    this.ydoc.transact(() => {
      const currentLength = this.ytext.toString().length;
      this.ytext.delete(0, currentLength);
      this.ytext.insert(0, content);
    });
}

  // 销毁所有资源
  public destroy(): void {
    this.deactivate()
    this.ydoc.destroy()
    console.log('协作管理器已销毁')
  }
}
