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

    // Allocate a unique connection ID for echo prevention
    let connection_id = bcast.next_connection_id();

    let connection = RoomConnection::new(room_id.clone(), room_info, room_manager);

    let (mut sink, mut stream) = ws.split();

    // Send initial messages (SyncStep1 + SyncStep2 + AwarenessUpdate)
    let initial_messages = bcast.get_initial_messages().await;
    for msg in initial_messages {
        if let Err(e) = sink.send(Message::Binary(msg.into())).await {
            tracing::error!("发送初始化消息失败: {}", e);
            connection.cleanup().await;
            return;
        }
    }
    tracing::debug!("已向房间 {} 发送初始化消息", room_id);

    let mut broadcast_rx = bcast.subscribe();

    loop {
        tokio::select! {
            msg = stream.next() => {
                match msg {
                    Some(Ok(Message::Binary(data))) => {
                        let responses = bcast.handle_message(connection_id, &data).await;
                        for response in responses {
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
                    Some(Ok(_)) => {}
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
            broadcast_msg = broadcast_rx.recv() => {
                match broadcast_msg {
                    Ok(payload) => {
                        // Skip messages sent by this connection (echo prevention)
                        if payload.sender_id == connection_id {
                            continue;
                        }
                        if let Err(e) = sink.send(Message::Binary(payload.data.into())).await {
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
    connection.cleanup().await;
}