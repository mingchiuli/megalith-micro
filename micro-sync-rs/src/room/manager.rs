use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::time::{Duration, Instant};
use tokio::sync::{Mutex, RwLock};

use super::sync_protocol::BroadcastGroup;

// 房间连接包装器
pub struct RoomConnection {
    room_id: String,
    room_info: Arc<RoomInfo>,
    room_manager: Arc<RoomManager>,
}

impl RoomConnection {
    pub fn new(room_id: String, room_info: Arc<RoomInfo>, room_manager: Arc<RoomManager>) -> Self {
        RoomConnection {
            room_id,
            room_info,
            room_manager,
        }
    }

    pub fn room_id(&self) -> &str {
        &self.room_id
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
    empty_since: Mutex<Option<Instant>>,
}

impl RoomInfo {
    async fn join(&self, room_id: &str) {
        self.connection_count.fetch_add(1, Ordering::SeqCst);
        if self.empty_since.lock().await.take().is_some() {
            tracing::info!("房间 {} 在被删除前有新用户加入，取消删除", room_id);
        }
    }
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

    /// 获取或创建房间
    pub async fn get_or_create_room(
        &self, // 改为 &self，因为用的是 RwLock
        room_id: &str,
    ) -> (Arc<RoomInfo>, Arc<BroadcastGroup>) {
        // 先尝试读锁获取房间
        {
            let rooms = self.rooms.read().await;
            if let Some(room) = rooms.get(room_id) {
                room.join(room_id).await;

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
            room.join(room_id).await;

            tracing::info!(
                "用户加入已存在房间 {} (double-check). 当前连接数: {}",
                room_id,
                room.connection_count.load(Ordering::SeqCst)
            );

            return (room.clone(), room.broadcast_group.clone());
        }

        // 确认不存在，创建新房间
        tracing::info!("创建新房间: {}", room_id);

        let broadcast_group = Arc::new(BroadcastGroup::new(32));

        let room_info = Arc::new(RoomInfo {
            broadcast_group: broadcast_group.clone(),
            connection_count: AtomicUsize::new(1),
            empty_since: Mutex::new(None),
        });

        rooms.insert(room_id.to_string(), room_info.clone());

        tracing::info!("创建新房间完成: {}. 当前连接数: 1", room_id);

        (room_info, broadcast_group)
    }

    /// 用户离开房间
    async fn leave_room(&self, room_id: &str, room_info: &Arc<RoomInfo>) -> bool {
        let previous_count =
            room_info
                .connection_count
                .fetch_update(Ordering::SeqCst, Ordering::SeqCst, |count| {
                    count.checked_sub(1)
                });
        let Ok(previous_count) = previous_count else {
            tracing::warn!("忽略房间 {} 的重复离开请求", room_id);
            return false;
        };
        let current_count = previous_count - 1;

        tracing::info!("用户离开房间 {}. 剩余连接数: {}", room_id, current_count);

        if current_count == 0 {
            // 不立即删除，标记删除时间
            let mut empty_since = room_info.empty_since.lock().await;
            *empty_since = Some(Instant::now());

            tracing::info!(
                "房间 {} 已无用户，标记为待删除（将在 5 分钟后删除）",
                room_id
            );

            return false;
        }

        false
    }

    /// 定期清理过期的空房间
    pub async fn cleanup_expired_rooms(&self) -> usize {
        let now = Instant::now();
        let mut to_remove = Vec::new();

        // 先用读锁检查
        {
            let rooms = self.rooms.read().await;

            for (room_id, room_info) in rooms.iter() {
                let count = room_info.connection_count.load(Ordering::SeqCst);

                if count == 0 {
                    let empty_since = room_info.empty_since.lock().await;

                    if let Some(empty_time) = *empty_since {
                        let duration = now.duration_since(empty_time);
                        // 超过 5 分钟才删除
                        if duration >= Duration::from_secs(300) {
                            tracing::info!("房间 {} 已空闲 {:?}，准备清理", room_id, duration);
                            to_remove.push((room_id.clone(), room_info.clone(), empty_time));
                        }
                    }
                }
            }
        }

        self.remove_expired_candidates(now, to_remove).await
    }

    async fn remove_expired_candidates(
        &self,
        now: Instant,
        candidates: Vec<(String, Arc<RoomInfo>, Instant)>,
    ) -> usize {
        if !candidates.is_empty() {
            let mut rooms = self.rooms.write().await;
            let mut removed_count = 0;

            for (room_id, candidate, observed_empty_since) in candidates {
                let should_remove = if let Some(current) = rooms.get(&room_id) {
                    if !Arc::ptr_eq(current, &candidate)
                        || current.connection_count.load(Ordering::SeqCst) != 0
                    {
                        false
                    } else {
                        let empty_since = current.empty_since.lock().await;
                        empty_since.is_some_and(|empty_time| {
                            empty_time == observed_empty_since
                                && now.duration_since(empty_time) >= Duration::from_secs(300)
                        })
                    }
                } else {
                    false
                };

                if should_remove {
                    rooms.remove(&room_id);
                    removed_count += 1;
                    tracing::info!("已删除房间: {}", room_id);
                }
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

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn new_room_manager_has_no_rooms() {
        let rm = RoomManager::new();
        assert!(rm.rooms.read().await.is_empty());
    }

    #[tokio::test]
    async fn default_constructor_has_no_rooms() {
        let rm = RoomManager::default();
        assert!(rm.rooms.read().await.is_empty());
    }

    #[tokio::test]
    async fn cleanup_returns_zero_when_no_rooms() {
        let rm = RoomManager::new();
        assert_eq!(rm.cleanup_expired_rooms().await, 0);
    }

    #[tokio::test]
    async fn cleanup_removes_an_expired_empty_room() {
        let rm = Arc::new(RoomManager::new());
        let (room, _) = rm.get_or_create_room("expired").await;
        rm.leave_room("expired", &room).await;
        *room.empty_since.lock().await = Some(Instant::now() - Duration::from_secs(301));

        assert_eq!(rm.cleanup_expired_rooms().await, 1);
        assert!(!rm.rooms.read().await.contains_key("expired"));
    }

    #[tokio::test]
    async fn cleanup_keeps_a_room_that_was_rejoined() {
        let rm = Arc::new(RoomManager::new());
        let (room, _) = rm.get_or_create_room("rejoined").await;
        rm.leave_room("rejoined", &room).await;
        *room.empty_since.lock().await = Some(Instant::now() - Duration::from_secs(301));

        let (rejoined, _) = rm.get_or_create_room("rejoined").await;

        assert!(Arc::ptr_eq(&room, &rejoined));
        assert_eq!(rm.cleanup_expired_rooms().await, 0);
        assert!(rm.rooms.read().await.contains_key("rejoined"));
    }

    #[tokio::test]
    async fn duplicate_leave_does_not_underflow_connection_count() {
        let rm = Arc::new(RoomManager::new());
        let (room, _) = rm.get_or_create_room("duplicate-leave").await;

        rm.leave_room("duplicate-leave", &room).await;
        rm.leave_room("duplicate-leave", &room).await;

        assert_eq!(room.connection_count.load(Ordering::SeqCst), 0);
    }

    #[tokio::test]
    async fn cleanup_does_not_remove_a_replacement_with_the_same_room_id() {
        let rm = Arc::new(RoomManager::new());
        let (candidate, _) = rm.get_or_create_room("replaced").await;
        rm.leave_room("replaced", &candidate).await;
        let expired_at = Instant::now() - Duration::from_secs(301);
        *candidate.empty_since.lock().await = Some(expired_at);

        let replacement = Arc::new(RoomInfo {
            broadcast_group: Arc::new(BroadcastGroup::new(4)),
            connection_count: AtomicUsize::new(1),
            empty_since: Mutex::new(None),
        });
        rm.rooms
            .write()
            .await
            .insert("replaced".to_string(), replacement.clone());

        let removed = rm
            .remove_expired_candidates(
                Instant::now(),
                vec![("replaced".to_string(), candidate, expired_at)],
            )
            .await;

        assert_eq!(removed, 0);
        assert!(Arc::ptr_eq(
            rm.rooms.read().await.get("replaced").unwrap(),
            &replacement
        ));
    }
}
