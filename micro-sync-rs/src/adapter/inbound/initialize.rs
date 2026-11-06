use crate::application::port::CollaborationStore;
use crate::application::room_manager::RoomManager;
use axum::{
    Json,
    extract::{Path, State},
    http::{HeaderMap, StatusCode},
};
use serde::Deserialize;
use std::sync::Arc;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;

#[derive(Deserialize)]
pub(crate) struct InitializeRoom {
    content: String,
}

pub(crate) async fn initialize<S: CollaborationStore>(
    Path(room_id): Path<String>,
    State(manager): State<Arc<RoomManager<S>>>,
    headers: HeaderMap,
    Json(body): Json<InitializeRoom>,
) -> Result<StatusCode, StatusCode> {
    if room_id.len() > 128
        || !room_id
            .bytes()
            .all(|c| c.is_ascii_alphanumeric() || matches!(c, b':' | b'-'))
    {
        return Err(StatusCode::BAD_REQUEST);
    }
    let parent = opentelemetry::global::get_text_map_propagator(|propagator| {
        propagator.extract(&crate::extractor::AxumHeaderExtractor(&headers))
    });
    let span = tracing::info_span!("initialize_collaboration_room");
    let _ = span.set_parent(parent);
    manager
        .initialize_room(&room_id, &body.content)
        .instrument(span)
        .await
        .map_err(|error| {
            tracing::warn!(%error, "failed to initialize collaboration room");
            StatusCode::SERVICE_UNAVAILABLE
        })?;
    Ok(StatusCode::NO_CONTENT)
}
