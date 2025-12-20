use opentelemetry::global;
use opentelemetry::metrics::Counter;
use warp::reply::Reply;

use std::sync::Arc;
use tokio::sync::Mutex;
use warp::Filter;
use warp::filters::header::headers_cloned;
use warp::http::HeaderMap;

use crate::{
    config::config::{self, ConfigKey},
    room::{RoomManager, check_room_exists, ws_handler},
};

pub fn set_route() -> impl Filter<Extract = impl Reply, Error = warp::Rejection> + Clone {
    let room_manager = Arc::new(Mutex::new(RoomManager::new()));
    let room_manager_ws = room_manager.clone();
    let room_manager_rooms = room_manager.clone();

    // WebSocket 路由 - 提取 headers 用于链路追踪
    let ws_routes = warp::path("rooms")
        .and(warp::path::param::<String>())
        .and(warp::header::headers_cloned())
        .and(warp::ws())
        .and(warp::any().map(move || room_manager_ws.clone()))
        .and_then(ws_handler);

    // 简单的健康检查路由
    let health_check = warp::path("actuator")
        .and(warp::path("health"))
        .and(warp::get())
        .map(|| "OK");

    // Create metrics
    let meter = global::meter(config::get_static_value(ConfigKey::ServerName));

    let request_counter: Counter<u64> = meter
        .u64_counter("http_requests_total")
        .with_description("Total number of HTTP requests")
        .build();

    let check_rooms = warp::path("rooms")
        .and(warp::path("exist"))
        .and(warp::path::param::<String>())
        .and(warp::get())
        .and(headers_cloned())
        .and_then(move |room_id: String, headers: HeaderMap| {
            // 新增：接收 headers 参数
            let room_manager = room_manager_rooms.clone();
            let counter = request_counter.clone();
            async move { check_room_exists(room_id, room_manager, counter, headers).await }
        });

    // 合并所有路由
    ws_routes.or(health_check).or(check_rooms)
}
