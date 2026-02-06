use crate::extractor::AxumHeaderExtractor;
use crate::room::room::{RoomConnection, RoomManager};
use axum::extract::ws::{Message, WebSocket, WebSocketUpgrade};
use axum::extract::{Path, State};
use axum::response::IntoResponse;
use futures_util::{SinkExt, StreamExt};
use axum::http::HeaderMap;
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
    // 从请求头中提取 trace context
    let parent_context = global::get_text_map_propagator(|propagator| {
        propagator.extract(&AxumHeaderExtractor(&headers))
    });

    // 创建带有父 context 的 span
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

    // 获取或创建此房间的广播组，同时获取房间信息引用
    let (room_info, bcast) = room_manager.get_or_create_room(&room_id).await;

    // 创建连接信息对象，用于管理生命周期
    let connection = RoomConnection::new(room_id.clone(), room_info, room_manager);

    let (mut sink, mut stream) = ws.split();

    // 🔑 发送 SyncStep1（对新连接和重连都安全）
    let sync_step1 = bcast.create_sync_step1().await;
    if let Err(e) = sink.send(Message::Binary(sync_step1.into())).await {
        tracing::error!("发送 SyncStep1 失败: {}", e);
        connection.cleanup().await;
        return;
    }
    tracing::debug!("已向房间 {} 发送 SyncStep1", room_id);

    // 发送当前 awareness 状态
    if let Some(awareness_update) = bcast.create_awareness_update().await {
        if let Err(e) = sink.send(Message::Binary(awareness_update.into())).await {
            tracing::error!("发送 awareness 更新失败: {}", e);
            connection.cleanup().await;
            return;
        }
    }

    // 订阅广播消息
    let mut broadcast_rx = bcast.subscribe();

    // 主循环：处理来自客户端的消息和广播消息
    loop {
        tokio::select! {
            // 处理来自客户端的消息
            msg = stream.next() => {
                match msg {
                    Some(Ok(Message::Binary(data))) => {
                        // 处理消息并获取可能的响应
                        if let Some(response) = bcast.handle_message(&data).await {
                            if let Err(e) = sink.send(Message::Binary(response.into())).await {
                                tracing::error!("发送响应失败: {}", e);
                                break;
                            }
                        }
                    }
                    Some(Ok(Message::Close(_))) => {
                        tracing::info!("客户端关闭连接: {}", room_id);
                        break;
                    }
                    Some(Ok(Message::Ping(data))) => {
                        if let Err(e) = sink.send(Message::Pong(data)).await {
                            tracing::error!("发送 Pong 失败: {}", e);
                            break;
                        }
                    }
                    Some(Ok(_)) => {
                        // 忽略其他消息类型
                    }
                    Some(Err(e)) => {
                        tracing::error!("WebSocket 错误: {}", e);
                        break;
                    }
                    None => {
                        tracing::info!("WebSocket 流结束: {}", room_id);
                        break;
                    }
                }
            }
            // 处理广播消息
            broadcast_msg = broadcast_rx.recv() => {
                match broadcast_msg {
                    Ok(data) => {
                        if let Err(e) = sink.send(Message::Binary(data.into())).await {
                            tracing::error!("发送广播消息失败: {}", e);
                            break;
                        }
                    }
                    Err(tokio::sync::broadcast::error::RecvError::Lagged(n)) => {
                        tracing::warn!("广播接收器落后 {} 条消息", n);
                    }
                    Err(tokio::sync::broadcast::error::RecvError::Closed) => {
                        tracing::info!("广播通道已关闭");
                        break;
                    }
                }
            }
        }
    }

    tracing::info!("房间 {} 的连接已关闭", room_id);

    // 连接关闭时清理资源
    connection.cleanup().await;
}
