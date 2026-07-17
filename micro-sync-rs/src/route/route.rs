use axum::routing::get;
use axum::Router;
use std::sync::Arc;

use crate::{
    room::{ws_handler, RoomManager},
    schedule::task::start_cleanup_task,
};

pub fn set_route() -> Router {
    let room_manager = Arc::new(RoomManager::new());
    start_cleanup_task(room_manager.clone());

    Router::new()
        // WebSocket 路由
        .route("/rooms/{room_id}", get(ws_handler))
        .with_state(room_manager)
        // 健康检查路由
        .route("/actuator/health", get(|| async { "OK" }))
}
