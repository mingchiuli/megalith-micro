use opentelemetry::metrics::Counter;
use opentelemetry::{KeyValue, global};

use serde_json::json;
use std::sync::Arc;
use tokio::sync::Mutex;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use warp::http::HeaderMap;
use warp::reject::Rejection;
use warp::reply::json;

use crate::extractor::WarpHeaderExtractor;
use crate::room::room::RoomManager;

pub async fn check_room_exists(
    room_id: String,
    room_manager: Arc<Mutex<RoomManager>>,
    counter: Counter<u64>,
    headers: HeaderMap,
) -> Result<warp::reply::Json, Rejection> {
    // 1. 提取上游 Trace Context
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&WarpHeaderExtractor(&headers))
    });

    // 2. 创建 Span 并关联父上下文
    let span = tracing::info_span!(
        "websocket_connection",
        room_id = %room_id,
        otel.name = format!("WebSocket: /rooms/{}", room_id)
    );
    let _ = span.set_parent(parent_context);

    // 3. 核心修复：用 instrument 包裹所有异步逻辑（替代 span.entered()）
    let result = async move {
        // 业务逻辑：跨 await 完全安全（instrument 保证 Span 上下文自动传播）
        counter.add(1, &[KeyValue::new("endpoint", "check_room_exists")]);
        let room_manager = room_manager.lock().await; // 无编译错误
        let exists = room_manager.room_exists(&room_id);
        tracing::info!(room_id = %room_id, exists = %exists, "Checking room existence");

        Ok(json(&json!({
            "code": 200,
            "msg": "success",
            "data": {
                "exists": exists
            }
        })))
    }
    .instrument(span)
    .await; // instrument 满足 Send，跨 await 安全

    result
}
