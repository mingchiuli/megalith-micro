use std::sync::Arc;

use axum::extract::State;
use axum::http::StatusCode;
use axum::response::IntoResponse;

use crate::application::RoomManager;
use crate::application::port::CollaborationStore;

pub(crate) async fn health<S>(State(room_manager): State<Arc<RoomManager<S>>>) -> impl IntoResponse
where
    S: CollaborationStore,
{
    if room_manager.is_ready() {
        (StatusCode::OK, "OK")
    } else {
        (StatusCode::SERVICE_UNAVAILABLE, "Redis unavailable")
    }
}
