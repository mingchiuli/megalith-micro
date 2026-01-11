use std::{sync::Arc, time::Duration};

use crate::room::RoomManager;

/// 启动定时清理任务
pub fn start_cleanup_task(room_manager: Arc<RoomManager>) {
    tokio::spawn(async move {
        let mut interval = tokio::time::interval(Duration::from_secs(60));
        
        loop {
            interval.tick().await;
            
            let removed = room_manager.cleanup_expired_rooms().await;
            
            if removed > 0 {
                tracing::info!("定时任务：清理了 {} 个过期房间", removed);
            }
        }
    });
}