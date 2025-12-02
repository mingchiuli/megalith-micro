use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicUsize, Ordering};
use tokio::sync::Mutex;
use yrs::Doc;
use yrs::sync::Awareness;
use yrs_warp::broadcast::BroadcastGroup;

// 房间信息
pub struct RoomConnection {
    room_id: String,
    room_info: Arc<RoomInfo>,
    room_manager: Arc<Mutex<RoomManager>>,
}

impl RoomConnection {
    // 在连接关闭时自动清理
    pub async fn cleanup(self) {
        let mut manager = self.room_manager.lock().await;
        manager.leave_room(&self.room_id, self.room_info);
    }

    pub fn new(
        room_id: String,
        room_info: Arc<RoomInfo>,
        room_manager: Arc<Mutex<RoomManager>>,
    ) -> Self {
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

// 房间管理器
pub struct RoomManager {
    rooms: HashMap<String, Arc<RoomInfo>>,
}

impl RoomManager {
    pub fn new() -> Self {
        RoomManager {
            rooms: HashMap::new(),
        }
    }

    // 检查房间是否存在
    pub fn room_exists(&self, room_id: &str) -> bool {
        match self.rooms.get(room_id) {
            Some(room) => room.connection_count.load(Ordering::SeqCst) > 1,
            None => false,
        }
    }

    // 获取或创建房间
    pub async fn get_or_create_room(
        &mut self,
        room_id: &str,
    ) -> (Arc<RoomInfo>, Arc<BroadcastGroup>) {
        if let Some(room) = self.rooms.get(room_id) {
            room.connection_count.fetch_add(1, Ordering::SeqCst);
            tracing::info!(
                "用户加入已存在房间 {}. 当前连接数: {}",
                room_id,
                room.connection_count.load(Ordering::SeqCst)
            );
            (room.clone(), room.broadcast_group.clone())
        } else {
            // 创建新房间
            let doc = Doc::new();

            let awareness = Arc::new(Awareness::new(doc));

            let broadcast_group = Arc::new(BroadcastGroup::new(awareness, 32).await);

            let room_info = Arc::new(RoomInfo {
                broadcast_group: broadcast_group.clone(),
                connection_count: AtomicUsize::new(1),
            });

            self.rooms.insert(room_id.to_string(), room_info.clone());
            tracing::info!("创建新房间: {}", room_id);

            (room_info, broadcast_group)
        }
    }

    // 用户离开房间
    pub fn leave_room(&mut self, room_id: &str, room_info: Arc<RoomInfo>) -> bool {
        let prev_count = room_info.connection_count.fetch_sub(1, Ordering::SeqCst);
        let current_count = prev_count - 1;

        tracing::info!("用户离开房间 {}. 剩余连接数: {}", room_id, current_count);

        if current_count == 0 {
            tracing::info!("用户全部离开房间:{}", room_id);
            self.rooms.remove(room_id);
        }

        true
    }
}
