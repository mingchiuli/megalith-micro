use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicUsize, Ordering};
use tokio::sync::RwLock;
use yrs::Doc;
use yrs::sync::Awareness;
use yrs_warp::broadcast::BroadcastGroup;

// 房间信息
pub struct RoomConnection {
    room_id: String,
    room_info: Arc<RoomInfo>,
    room_manager: Arc<RoomManager>,
}

impl RoomConnection {
    // 在连接关闭时自动清理
    pub async fn cleanup(self) {
        self.room_manager
            .leave_room(&self.room_id, &self.room_info)
            .await;
    }

    pub fn new(room_id: String, room_info: Arc<RoomInfo>, room_manager: Arc<RoomManager>) -> Self {
        RoomConnection {
            room_id,
            room_info,
            room_manager,
        }
    }
}

// 房间信息结构
pub struct RoomInfo {
    broadcast_group: Arc<BroadcastGroup>,
    connection_count: AtomicUsize, // 使用原子计数器确保线程安全
}

// 房间管理器 - 使用 RwLock 实现细粒度锁
// 读操作（获取房间）可以并发，只有创建/删除房间时需要写锁
pub struct RoomManager {
    rooms: RwLock<HashMap<String, Arc<RoomInfo>>>,
}

impl RoomManager {
    pub fn new() -> Self {
        RoomManager {
            rooms: RwLock::new(HashMap::new()),
        }
    }

    // 检查房间是否存在
    pub async fn room_exists(&self, room_id: &str) -> bool {
        let rooms = self.rooms.read().await;
        rooms.contains_key(room_id)
    }

    // 获取或创建房间
    pub async fn get_or_create_room(
        &self,
        room_id: &str,
    ) -> (Arc<RoomInfo>, Arc<BroadcastGroup>) {
        // 先尝试用读锁获取已存在的房间
        {
            let rooms = self.rooms.read().await;
            if let Some(room) = rooms.get(room_id) {
                room.connection_count.fetch_add(1, Ordering::SeqCst);
                tracing::info!(
                    "用户加入已存在房间 {}. 当前连接数: {}",
                    room_id,
                    room.connection_count.load(Ordering::SeqCst)
                );
                return (room.clone(), room.broadcast_group.clone());
            }
        }

        // 房间不存在，需要写锁创建新房间
        let mut rooms = self.rooms.write().await;

        // 双重检查：可能在等待写锁期间其他线程已创建
        if let Some(room) = rooms.get(room_id) {
            room.connection_count.fetch_add(1, Ordering::SeqCst);
            tracing::info!(
                "用户加入已存在房间 {}. 当前连接数: {}",
                room_id,
                room.connection_count.load(Ordering::SeqCst)
            );
            return (room.clone(), room.broadcast_group.clone());
        }

        // 创建新房间
        let doc = Doc::new();
        let awareness = Arc::new(Awareness::new(doc));
        let broadcast_group = Arc::new(BroadcastGroup::new(awareness, 32).await);

        let room_info = Arc::new(RoomInfo {
            broadcast_group: broadcast_group.clone(),
            connection_count: AtomicUsize::new(1),
        });

        rooms.insert(room_id.to_string(), room_info.clone());
        tracing::info!("创建新房间: {}", room_id);

        (room_info, broadcast_group)
    }

    /// 用户离开房间
    pub async fn leave_room(&self, room_id: &str, room_info: &Arc<RoomInfo>) {
        let prev_count = room_info.connection_count.fetch_sub(1, Ordering::SeqCst);
        let current_count = prev_count.saturating_sub(1);

        tracing::info!("用户离开房间 {}. 剩余连接数: {}", room_id, current_count);

        // 当连接数降到 0 时清理房间
        if current_count == 0 {
            let mut rooms = self.rooms.write().await;
            // 再次检查连接数，确保在获取写锁期间没有新连接加入
            if room_info.connection_count.load(Ordering::SeqCst) == 0 {
                rooms.remove(room_id);
                tracing::info!("房间 {} 已无用户，执行清理", room_id);
            }
        }
    }
}
