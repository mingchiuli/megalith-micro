use axum::extract::{Path, State};
use axum::Json;
use axum::http::HeaderMap;
use opentelemetry::metrics::Counter;
use opentelemetry::{global, KeyValue};
use serde_json::{json, Value};
use std::sync::Arc;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::extractor::AxumHeaderExtractor;
use crate::room::room::RoomManager;

pub struct RoomCheckerState {
    pub room_manager: Arc<RoomManager>,
    pub counter: Counter<u64>,
}

pub async fn check_room_exists(
    Path(room_id): Path<String>,
    State(state): State<Arc<RoomCheckerState>>,
    headers: HeaderMap,
) -> Json<Value> {
    // 1. 提取上游 Trace Context
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&AxumHeaderExtractor(&headers))
    });

    // 2. 创建 Span 并关联父上下文
    let span = tracing::info_span!(
        "check_room_exists",
        room_id = %room_id,
        otel.name = format!("GET /rooms/exist/{}", room_id)
    );
    let _ = span.set_parent(parent_context);

    // 3. 用 instrument 包裹所有逻辑
    async move {
        state
            .counter
            .add(1, &[KeyValue::new("endpoint", "check_room_exists")]);
        let exists = state.room_manager.room_exists(&room_id).await;
        tracing::info!(room_id = %room_id, exists = %exists, "Checking room existence");

        Json(json!({
            "code": 200,
            "msg": "success",
            "data": {
                "exists": exists
            }
        }))
    }
    .instrument(span)
    .await
}
