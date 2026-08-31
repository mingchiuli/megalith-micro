use axum::Router;
use axum::routing::get;
use std::time::Duration;

use crate::adapter::inbound::health::health;
use crate::adapter::inbound::websocket::ws_handler;
use crate::adapter::outbound::redis::RedisSessionStore;
use crate::application::{RoomManager, RoomManagerOptions};
use crate::config::{redis_config, sync_config, worker_config};

pub async fn set_route() -> redis::RedisResult<Router> {
    let sync = sync_config();
    let worker = worker_config();
    let options = RoomManagerOptions {
        connection_buffer: sync.connection_buffer,
        initial_sync_timeout: Duration::from_secs(sync.initial_sync_timeout_seconds.max(1)),
        lease_heartbeat: Duration::from_secs(sync.lease_heartbeat_seconds.max(1)),
        worker_task_debounce: Duration::from_secs(worker.task_debounce_seconds),
        worker_task_timeout: Duration::from_secs(worker.task_timeout_seconds),
        worker_concurrency: worker.concurrency,
    };
    let store = RedisSessionStore::connect(redis_config(), sync, worker).await?;
    store.ensure_worker_group().await?;
    let room_manager = RoomManager::new(store, options);
    room_manager.start();

    Ok(Router::new()
        .route("/rooms/{room_id}", get(ws_handler::<RedisSessionStore>))
        .route("/actuator/health", get(health::<RedisSessionStore>))
        .with_state(room_manager))
}
