use axum::routing::get;
use axum::Router;
use opentelemetry::global;
use std::sync::Arc;

use crate::{
    config::config::{self, ConfigKey},
    room::{check_room_exists, ws_handler, RoomCheckerState, RoomManager},
    schedule::task::start_cleanup_task,
};

pub fn set_route() -> Router {
    let room_manager = Arc::new(RoomManager::new());
    start_cleanup_task(room_manager.clone());

    // Create metrics
    let meter = global::meter(config::get_static_value(ConfigKey::ServerName));

    let request_counter = meter
        .u64_counter("http_requests_total")
        .with_description("Total number of HTTP requests")
        .build();

    let room_checker_state = Arc::new(RoomCheckerState {
        room_manager: room_manager.clone(),
        counter: request_counter,
    });

    Router::new()
        // WebSocket 路由
        .route("/rooms/{room_id}", get(ws_handler))
        .with_state(room_manager)
        // 健康检查路由
        .route("/actuator/health", get(|| async { "OK" }))
        // 检查房间是否存在
        .route(
            "/rooms/exist/{room_id}",
            get(check_room_exists).with_state(room_checker_state),
        )
}
