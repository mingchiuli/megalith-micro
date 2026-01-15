use crate::extractor::WarpHeaderExtractor;
use crate::room::room::{RoomConnection, RoomManager};
use futures_util::{SinkExt, StreamExt};
use opentelemetry::global;
use yrs::{ReadTxn, Transact};
use yrs::sync::{Message, SyncMessage};
use yrs::updates::encoder::Encode;
use std::sync::Arc;
use tokio::sync::Mutex;
use tracing::Instrument;
use tracing_opentelemetry::OpenTelemetrySpanExt;
use warp::filters::ws::{WebSocket, Ws};
use warp::http::HeaderMap;
use warp::{Rejection, Reply};
use yrs_warp::ws::{WarpSink, WarpStream};

pub async fn ws_handler(
    room_id: String,
    headers: HeaderMap,
    ws: Ws,
    room_manager: Arc<RoomManager>,
) -> Result<impl Reply, Rejection> {
    // 从请求头中提取 trace context
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&WarpHeaderExtractor(&headers))
    });

    // 创建带有父 context 的 span
    let span = tracing::info_span!(
        "websocket_connection",
        room_id = %room_id,
        otel.name = format!("WebSocket: /rooms/{}", room_id)
    );
    let _ = span.set_parent(parent_context);

    tracing::info!("新连接尝试加入房间: {}", room_id);
    let room_id_clone = room_id.clone();

    Ok(ws.on_upgrade(move |socket| peer(socket, room_manager, room_id_clone).instrument(span)))
}

async fn peer(ws: WebSocket, room_manager: Arc<RoomManager>, room_id: String) {
    tracing::info!("已建立新连接到房间: {}", room_id);

    // 获取或创建此房间的广播组，同时获取房间信息引用
    let (room_info, bcast) = room_manager.get_or_create_room(&room_id).await;

    // 创建连接信息对象，用于管理生命周期
    let connection = RoomConnection::new(room_id.clone(), room_info, room_manager);

    let (sink, stream) = ws.split();
    let sink = Arc::new(Mutex::new(WarpSink::from(sink)));
    let stream = WarpStream::from(stream);

    // 🔑 发送 SyncStep1（对新连接和重连都安全）
    {
        let awareness = bcast.awareness();
        let sv = awareness.doc().transact().state_vector();
        let msg = Message::Sync(SyncMessage::SyncStep1(sv));
        let payload = msg.encode_v1();
        
        let mut sink_guard = sink.lock().await;
        if let Err(e) = sink_guard.send(payload).await {
            tracing::error!("发送 SyncStep1 失败: {}", e);
            // 发送失败，提前退出
            connection.cleanup().await;
            return;
        }
        tracing::debug!("已向房间 {} 发送 SyncStep1", room_id);
    }

    let sub = bcast.subscribe(sink, stream);

    // 等待连接关闭
    match sub.completed().await {
        Ok(_) => tracing::info!("房间 {} 的通道广播成功完成", room_id),
        Err(e) => tracing::error!("房间 {} 的通道广播异常终止: {}", room_id, e),
    }

    // 连接关闭时清理资源
    connection.cleanup().await;
}
