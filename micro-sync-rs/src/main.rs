use micro_sync_rs::handler::broadcast::ws_handler;
use micro_sync_rs::room::room::RoomManager;
use serde_json::json;
use std::env;
use std::sync::Arc;
use tokio::signal;
use tokio::sync::Mutex;
use warp::Filter;
use warp::reject::Rejection;

const LOGO: &str = r#"
 _    _  ___  ____  ____
| |  | |/ _ \|  _ \|  _ \
| |/\| | |_| | |_) | |_) |
\  /\  /  _  |  _ <|  __/
 \/  \/_/ \_\_| \_\_|
"#;

#[tokio::main]
async fn main() {
    // Initialize logging
    if env::var("RUST_LOG").is_err() {
        unsafe {
            env::set_var("RUST_LOG", "info");
        }
    }
    env_logger::init();

    log::info!("{}", LOGO);

    let room_manager = Arc::new(Mutex::new(RoomManager::new()));
    let room_manager_ws = room_manager.clone();
    let room_manager_rooms = room_manager.clone();

    // WebSocket 路由
    let ws_routes = warp::path("rooms")
        .and(warp::path::param::<String>())
        .and(warp::ws())
        .and(warp::any().map(move || room_manager_ws.clone()))
        .and_then(ws_handler);

    // 简单的健康检查路由
    let health_check = warp::path("actuator")
        .and(warp::path("health"))
        .and(warp::get())
        .map(|| "OK");

    let check_rooms = warp::path("rooms")
        .and(warp::path("exist"))
        .and(warp::path::param::<String>())
        .and(warp::get())
        .and_then(move |room_id: String| {
            let room_manager = room_manager_rooms.clone();
            async move {
                let room_manager = room_manager.lock().await;
                let exists = room_manager.room_exists(&room_id);
                Ok::<_, Rejection>(warp::reply::json(&json!({
                    "code": 200,
                    "msg": "success",
                    "data": {
                        "exists": exists,
                        "roomId": room_id
                    }
                })))
            }
        });

    // 合并所有路由
    let routes = ws_routes.or(health_check).or(check_rooms);

    // 创建服务器任务
    let (_, server) =
        warp::serve(routes).bind_with_graceful_shutdown(([0, 0, 0, 0], 8089), shutdown_signal());

    // 等待服务器运行完成
    server.await;

    log::info!("Server shutdown completed");
}

async fn shutdown_signal() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("Failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("Failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();
    tokio::select! {
        _ = ctrl_c => {
            log::info!("Ctrl-C received");
        },
        _ = terminate => {
            log::info!("Terminate signal received");
        },
    }
    log::info!("Shutdown signal received");
}
