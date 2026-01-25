use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::time::{Duration, SystemTime};
use tokio::sync::{Mutex, RwLock};
use yrs::Doc;
use yrs::sync::Awareness;
use yrs_warp::broadcast::BroadcastGroup;

// 房间连接包装器
pub struct RoomConnection {
    room_id: String,
    room_info: Arc<RoomInfo>,
    room_manager: Arc<RoomManager>,
}

impl RoomConnection {
    pub fn new(
        room_id: String,
        room_info: Arc<RoomInfo>,
        room_manager: Arc<RoomManager>,
    ) -> Self {
        RoomConnection {
            room_id,
            room_info,
            room_manager,
        }
    }

    pub async fn cleanup(self) {
        self.room_manager
            .leave_room(&self.room_id, &self.room_info)
            .await;
    }
}

// 房间信息结构
pub struct RoomInfo {
    broadcast_group: Arc<BroadcastGroup>,
    connection_count: AtomicUsize,
    empty_since: Arc<Mutex<Option<SystemTime>>>,
}

// 房间管理器
pub struct RoomManager {
    rooms: RwLock<HashMap<String, Arc<RoomInfo>>>,
}

impl RoomManager {
    pub fn new() -> Self {
        RoomManager {
            rooms: RwLock::new(HashMap::new()),
        }
    }

    /// 检查房间是否存在
    pub async fn room_exists(&self, room_id: &str) -> bool {
        let rooms = self.rooms.read().await;
        rooms.contains_key(room_id)
    }

    /// 获取或创建房间
    pub async fn get_or_create_room(
        &self,  // 改为 &self，因为用的是 RwLock
        room_id: &str,
    ) -> (Arc<RoomInfo>, Arc<BroadcastGroup>) {
        // 先尝试读锁获取房间
        {
            let rooms = self.rooms.read().await;
            if let Some(room) = rooms.get(room_id) {
                room.connection_count.fetch_add(1, Ordering::SeqCst);
                
                // 取消删除标记
                let mut empty_since = room.empty_since.lock().await;
                if empty_since.is_some() {
                    tracing::info!(
                        "房间 {} 在被删除前有新用户加入，取消删除",
                        room_id
                    );
                    *empty_since = None;
                }
                drop(empty_since);
                
                tracing::info!(
                    "用户加入已存在房间 {}. 当前连接数: {}",
                    room_id,
                    room.connection_count.load(Ordering::SeqCst)
                );
                
                return (room.clone(), room.broadcast_group.clone());
            }
        } // 释放读锁

        // 房间不存在，获取写锁创建新房间
        let mut rooms = self.rooms.write().await;
        
        // Double-check：可能其他线程已经创建了
        if let Some(room) = rooms.get(room_id) {
            room.connection_count.fetch_add(1, Ordering::SeqCst);
            
            tracing::info!(
                "用户加入已存在房间 {} (double-check). 当前连接数: {}",
                room_id,
                room.connection_count.load(Ordering::SeqCst)
            );
            
            return (room.clone(), room.broadcast_group.clone());
        }
        
        // 确认不存在，创建新房间
        tracing::info!("创建新房间: {}", room_id);
        
        let doc = Doc::new();
        let awareness = Arc::new(Awareness::new(doc));
        let broadcast_group = Arc::new(BroadcastGroup::new(awareness, 32).await);
        
        let room_info = Arc::new(RoomInfo {
            broadcast_group: broadcast_group.clone(),
            connection_count: AtomicUsize::new(1),
            empty_since: Arc::new(Mutex::new(None)),
        });
        
        rooms.insert(room_id.to_string(), room_info.clone());
        
        tracing::info!(
            "创建新房间完成: {}. 当前连接数: 1",
            room_id
        );
        
        (room_info, broadcast_group)
    }

    /// 用户离开房间
    async fn leave_room(&self, room_id: &str, room_info: &Arc<RoomInfo>) -> bool {
        let prev_count = room_info.connection_count.fetch_sub(1, Ordering::SeqCst);
        let current_count = prev_count.saturating_sub(1);
        
        tracing::info!(
            "用户离开房间 {}. 剩余连接数: {}",
            room_id,
            current_count
        );
        
        if current_count == 0 {
            // 不立即删除，标记删除时间
            let mut empty_since = room_info.empty_since.lock().await;
            *empty_since = Some(SystemTime::now());
            
            tracing::info!(
                "房间 {} 已无用户，标记为待删除（将在 60 分钟后删除）",
                room_id
            );
            
            return false;
        }
        
        false
    }

    /// 定期清理过期的空房间
    pub async fn cleanup_expired_rooms(&self) -> usize {
        let now = SystemTime::now();
        let mut to_remove = Vec::new();
        
        // 先用读锁检查
        {
            let rooms = self.rooms.read().await;
            
            for (room_id, room_info) in rooms.iter() {
                let count = room_info.connection_count.load(Ordering::SeqCst);
                
                if count == 0 {
                    let empty_since = room_info.empty_since.lock().await;
                    
                    if let Some(empty_time) = *empty_since {
                        if let Ok(duration) = now.duration_since(empty_time) {
                            // 超过 60 分钟才删除
                            if duration >= Duration::from_secs(3600) {
                                tracing::info!(
                                    "房间 {} 已空闲 {:?}，准备清理",
                                    room_id,
                                    duration
                                );
                                to_remove.push(room_id.clone());
                            }
                        }
                    }
                }
            }
        }
        
        // 用写锁删除
        if !to_remove.is_empty() {
            let mut rooms = self.rooms.write().await;
            let removed_count = to_remove.len();
            
            for room_id in to_remove {
                rooms.remove(&room_id);
                tracing::info!("已删除房间: {}", room_id);
            }
            
            tracing::info!(
                "清理完成：删除了 {} 个过期房间，当前剩余 {} 个房间",
                removed_count,
                rooms.len()
            );
            
            return removed_count;
        }
        
        0
    }
}

impl Default for RoomManager {
    fn default() -> Self {
        Self::new()
    }
}

