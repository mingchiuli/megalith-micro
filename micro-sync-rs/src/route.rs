use axum::extract::State;
use axum::http::StatusCode;
use axum::routing::get;
use axum::{Router, response::IntoResponse};
use std::sync::Arc;

use crate::{
    config::{redis_config, sync_config, worker_config},
    room::{RedisSessionStore, RoomManager, ws_handler},
};

pub async fn set_route() -> redis::RedisResult<Router> {
    let store = RedisSessionStore::connect(redis_config(), sync_config(), worker_config()).await?;
    store.ensure_worker_group().await?;
    let room_manager = RoomManager::new(store);

    Ok(Router::new()
        .route("/rooms/{room_id}", get(ws_handler))
        .route("/actuator/health", get(health))
        .with_state(room_manager))
}

async fn health(State(room_manager): State<Arc<RoomManager>>) -> impl IntoResponse {
    if room_manager.is_ready() {
        (StatusCode::OK, "OK")
    } else {
        (StatusCode::SERVICE_UNAVAILABLE, "Redis unavailable")
    }
}
