use opentelemetry::metrics::Counter;
use opentelemetry::{KeyValue, global};

use serde_json::json;
use std::sync::Arc;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use warp::http::HeaderMap;
use warp::reject::Rejection;
use warp::reply::json;

use crate::extractor::WarpHeaderExtractor;
use crate::room::room::RoomManager;

pub async fn check_room_exists(
    room_id: String,
    room_manager: Arc<RoomManager>,
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

    // 3. 用 instrument 包裹所有逻辑
    let result = async move {
        counter.add(1, &[KeyValue::new("endpoint", "check_room_exists")]);
        let exists = room_manager.room_exists(&room_id).await;
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
    .await;

    result
}
