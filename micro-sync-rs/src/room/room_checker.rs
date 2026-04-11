use axum::extract::{Path, State};
use axum::Json;
use opentelemetry::metrics::Counter;
use opentelemetry::KeyValue;
use serde_json::{json, Value};
use std::sync::Arc;
use tracing::Instrument;

use crate::room::room::RoomManager;

pub struct RoomCheckerState {
    pub room_manager: Arc<RoomManager>,
    pub counter: Counter<u64>,
}

pub async fn check_room_exists(
    Path(room_id): Path<String>,
    State(state): State<Arc<RoomCheckerState>>,
) -> Json<Value> {
    let span = tracing::info_span!(
        "check_room_exists",
        room_id = %room_id,
        otel.name = format!("GET /rooms/exist/{}", room_id)
    );

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
