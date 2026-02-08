use crate::extractor::AxumHeaderExtractor;
use crate::room::room::{RoomConnection, RoomManager};
use axum::extract::ws::{WebSocket, WebSocketUpgrade};
use axum::extract::{Path, State};
use axum::http::HeaderMap;
use axum::response::IntoResponse;
use opentelemetry::global;
use std::sync::Arc;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;

pub async fn ws_handler(
    Path(room_id): Path<String>,
    State(room_manager): State<Arc<RoomManager>>,
    headers: HeaderMap,
    ws: WebSocketUpgrade,
) -> impl IntoResponse {
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&AxumHeaderExtractor(&headers))
    });

    let span = tracing::info_span!(
        "websocket_connection",
        room_id = %room_id,
        otel.name = format!("WebSocket: /rooms/{}", room_id)
    );
    let _ = span.set_parent(parent_context);

    tracing::info!("新连接尝试加入房间: {}", room_id);

    ws.on_upgrade(move |socket| peer(socket, room_manager, room_id).instrument(span))
}

async fn peer(ws: WebSocket, room_manager: Arc<RoomManager>, room_id: String) {
    tracing::info!("已建立新连接到房间: {}", room_id);

    let (room_info, bcast) = room_manager.get_or_create_room(&room_id).await;
    let connection = RoomConnection::new(room_id.clone(), room_info, room_manager);

    match bcast.subscribe(ws).await {
        Ok(()) => tracing::info!("房间 {} 的连接正常结束", room_id),
        Err(e) => tracing::error!("房间 {} 的连接异常结束: {}", room_id, e),
    }

    connection.cleanup().await;
}