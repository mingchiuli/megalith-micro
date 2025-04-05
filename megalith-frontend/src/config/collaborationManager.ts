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

// 协作管理器类
class CollaborationManager {
  private wsProvider: WebsocketProvider | null = null
  private indexeddbProvider: IndexeddbPersistence | null = null
  private isActive = false
  private activeRoomId = ''
  private userColor = { color: '#30bced', light: '#30bced33' }
  
  constructor() {
    // 随机选择用户颜色
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
    this.userColor = usercolors[random.uint32() % usercolors.length]
  }
  
  // 获取协作扩展，无论协作是否激活都能工作
  public getExtension() {
   
    return yCollab(ytext, this.wsProvider!.awareness, { undoManager })
  }
  
  // 检查是否需要切换房间
  private shouldSwitchRoom(roomId: string): boolean {
    return this.activeRoomId !== roomId
  }
  
  // 更新 CodeMirror 配置，使用当前的 awareness
  private updateCodeMirrorConfig() {
    // 获取适当的扩展
    const extension = this.getExtension()
    
    // 更新 CodeMirror 配置
    config({
      codeMirrorExtensions(_theme, extensions) { 
        // 添加新的 yCollab 扩展
        return [...extensions, extension]
      },
    })
    
    console.log('已更新 CodeMirror 协作配置')
  }
  
  // 激活协作功能，连接到服务器
  public async activate(roomId: string): Promise<boolean> {
    if (this.isActive && !this.shouldSwitchRoom(roomId)) {
      return true // 已经激活且在同一个房间
    }
    
    // 如果需要切换房间，先停用当前连接
    if (this.isActive) {
      this.deactivate()
    }
    
    const store = syncStore()
    store.room = roomId
    this.activeRoomId = roomId
    
    if (!store.url || !store.token) {
      console.warn('缺少必要的连接参数');
      return false;
    }
    
    try {
      console.log('正在激活WebSocket协作功能，连接房间:', roomId);
      
      // 初始化 WebSocket 提供者
      this.wsProvider = new WebsocketProvider(
        store.url,
        roomId,
        ydoc,
        {
          params: {
            token: store.token,
          }
        }
      )
      
      // 设置用户信息
      const userStr = localStorage.getItem('userinfo')
      if (userStr) {
        try {
          const loginUser: LoginStruct = JSON.parse(userStr)
          
          this.wsProvider.awareness.setLocalStateField('user', {
            name: loginUser.username,
            color: this.userColor.color,
            colorLight: this.userColor.light,
          })
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
      
      // 添加事件监听器
      this.wsProvider.on('status', (event) => {
        console.log('连接状态:', event.status);
      });
      
      this.wsProvider.on('connection-error', (error) => {
        console.error('连接错误:', error);
      });
      
      // 初始化 IndexedDB 持久化
      this.indexeddbProvider = new IndexeddbPersistence(roomId, ydoc)
      
      // 不管连接状态如何，立即更新 CodeMirror 配置
      // 这非常重要，确保 CodeMirror 使用新创建的 awareness
      this.updateCodeMirrorConfig()
      
      // 等待连接建立
      try {
        await new Promise<void>((resolve, reject) => {
          const timeout = setTimeout(() => {
            reject(new Error('连接超时'));
          }, 5000);
          
          const onSync = (isSynced: boolean) => {
            if (isSynced) {
              clearTimeout(timeout);
              this.wsProvider?.off('sync', onSync);
              resolve();
            }
          };
          
          this.wsProvider!.on('sync', onSync);
          
          if (this.wsProvider!.wsconnected) {
            clearTimeout(timeout);
            resolve();
          }
        });
      } catch (timeoutError) {
        console.warn('等待同步超时，但连接可能仍然有效');
        // 继续执行，不要中断流程
      }
      
      this.isActive = true;
      return true;
    } catch (error) {
      console.error('激活协作功能失败:', error);
      this.deactivate(); // 清理任何部分创建的资源
      return false;
    }
  }
  
  // 停用协作功能，断开连接
  public deactivate(): void {
    console.log('停用WebSocket协作功能');
    
    if (this.indexeddbProvider) {
      this.indexeddbProvider.destroy();
      this.indexeddbProvider = null;
    }
    
    if (this.wsProvider) {
      this.wsProvider.disconnect();
      this.wsProvider = null;
    }
    
    this.isActive = false;
    this.activeRoomId = '';
    
    // 更新配置，切换回独立模式
    this.updateCodeMirrorConfig();
  }
  
  // 获取文本内容
  public getText(): string {
    return ytext.toString();
  }
  
  // 设置文本内容
  public setText(content: string): void {
    ydoc.transact(() => {
      ytext.delete(0, ytext.toString().length);
      ytext.insert(0, content);
    });
  }
  
  // 返回文本绑定对象，用于外部引用
  public getYText() {
    return ytext;
  }
  
  // 获取当前状态
  public getStatus() {
    return {
      isActive: this.isActive,
      roomId: this.activeRoomId,
      connected: this.wsProvider?.wsconnected || false
    };
  }
}

// 创建单例实例
const collaborationManagerInstance = new CollaborationManager();
export { collaborationManagerInstance as collaborationManager, ytext, ydoc, undoManager };