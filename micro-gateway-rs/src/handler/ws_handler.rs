use axum::body::{Body, Bytes};
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::{CloseFrame, Message as AxumMessage, Utf8Bytes, WebSocket};
use axum::response::{IntoResponse, Response};
use futures_util::{SinkExt, StreamExt, stream};
use hyper::{HeaderMap, Method, Uri};
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::{
    Message as TungsteniteMessage, client::IntoClientRequest, protocol,
};

use crate::exception::error::HandlerError;
use crate::utils::constant;
use crate::utils::http_util::{self};

#[tracing::instrument(
    name = "proxy_websocket_request",
    skip(ws),
    fields(
        http.url = %uri,
    )
)]
pub async fn ws_route_handler(
    ws: WebSocketUpgrade,
    uri: Uri,
) -> Result<Response<Body>, HandlerError> {
    // Extract authentication token
    let token = extract_token(&uri);

    // Get authentication URL
    let auth_url = http_util::get_auth_url()?;

    // Prepare route request
    let req_body = http_util::prepare_route_request(&Method::GET, &HeaderMap::new(), &uri);

    // Forward to route service
    let route_resp = http_util::find_route(auth_url, req_body, &token).await?;

    // Parse url
    let new_url = http_util::parse_url(route_resp, &uri, constant::WS)?;

    // 将 WebSocket 升级响应转换为 Response<Body>
    let response = ws
        .on_upgrade(|socket| async move {
            handle_websocket_request(socket, new_url).await;
        })
        .into_response();

    let (parts, _) = response.into_parts();
    Ok(Response::from_parts(parts, Body::empty()))
}

fn extract_token(uri: &Uri) -> String {
    uri.query()
        .and_then(|q| {
            url::form_urlencoded::parse(q.as_bytes())
                .find(|(key, _)| key == constant::TOKEN)
                .map(|(_, value)| value.to_string())
        })
        .unwrap_or("".to_string())
}

async fn handle_websocket_request(ws: WebSocket, target_uri: Uri) {
    // 1. 构建 WebSocket 客户端请求（利用 IntoClientRequest 特性）
    let mut ws_request = match target_uri.into_client_request() {
        Ok(req) => req,
        Err(e) => {
            tracing::error!("Failed to build websocket request: {}", e);
            return;
        }
    };

    // 2. 获取请求头并注入 Trace Context（核心步骤）
    let headers = ws_request.headers_mut(); // 直接操作 tungstenite 的头
    http_util::inject_trace_context(headers); // 复用 HTTP 的注入逻辑

    // 3. 连接下游 WebSocket 服务（带 traceparent 头）
    let (server_ws, response) = match connect_async(ws_request).await {
        Ok((stream, resp)) => (stream, resp),
        Err(e) => {
            tracing::error!("Failed to connect to websocket service: {}", e);
            return;
        }
    };

    tracing::info!(
        "WebSocket connected to downstream, status: {}",
        response.status()
    );

    // 使用 futures_util 的 split 方法
    let (mut client_sender, mut client_receiver) = stream::StreamExt::split(ws);
    let (mut server_sender, mut server_receiver) = stream::StreamExt::split(server_ws);

    // 从客户端到服务端的消息转发
    let client_to_server = async {
        while let Some(msg) = client_receiver.next().await {
            match msg {
                Ok(msg) => {
                    // 将 axum WebSocket 消息转换为 tungstenite 消息
                    let converted_msg = match msg {
                        AxumMessage::Text(text) => TungsteniteMessage::text(text.as_str()),
                        AxumMessage::Binary(data) => TungsteniteMessage::binary(data.to_vec()),
                        AxumMessage::Ping(data) => TungsteniteMessage::Ping(data.into()),
                        AxumMessage::Pong(data) => TungsteniteMessage::Pong(data.into()),
                        AxumMessage::Close(Some(frame)) => {
                            // Convert from u16 to CloseCode
                            let code = protocol::frame::coding::CloseCode::from(frame.code);

                            // If there's a way to create a CloseFrame from a reason string
                            let reason_string = frame.reason.as_str().to_string();

                            // Try using a builder pattern or other constructor
                            // Without seeing the full API, we can attempt a few approaches:

                            // Option 1: If there's a with_reason or similar method
                            let close_frame = protocol::CloseFrame {
                                code,
                                reason: reason_string.into(), // Try with bytes directly
                            };

                            TungsteniteMessage::Close(Some(close_frame))
                        }
                        AxumMessage::Close(None) => TungsteniteMessage::Close(None),
                    };

                    if let Err(e) = server_sender.send(converted_msg).await {
                        tracing::error!("Error forwarding message to server: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    tracing::error!("Error receiving message from client: {}", e);
                    break;
                }
            }
        }
    };

    // 从服务端到客户端的消息转发
    let server_to_client = async {
        while let Some(msg) = server_receiver.next().await {
            match msg {
                Ok(msg) => {
                    // 将 tungstenite 消息转换为 axum WebSocket 消息
                    let converted_msg = match msg {
                        TungsteniteMessage::Text(text) => {
                            // 将 String 转换为 axum 的 Utf8Bytes
                            AxumMessage::Text(Utf8Bytes::from(text.as_str()))
                        }
                        TungsteniteMessage::Binary(data) => {
                            // 从 Vec<u8> 转为 axum 的 Bytes
                            AxumMessage::Binary(Bytes::from(data))
                        }
                        TungsteniteMessage::Ping(data) => AxumMessage::Ping(Bytes::from(data)),
                        TungsteniteMessage::Pong(data) => AxumMessage::Pong(Bytes::from(data)),
                        TungsteniteMessage::Close(frame) => {
                            match frame {
                                Some(close_frame) => {
                                    let code: u16 = close_frame.code.into();

                                    // Try to use methods on close_frame or reason to get a string or bytes
                                    // If reason is a custom UTF-8 type, it likely has methods to get the string content

                                    // Option 1: Try using the Display implementation if it exists
                                    let reason_str = format!("{}", close_frame.reason);

                                    // Option 2: If this is a tungstenite::protocol::CloseFrame
                                    // This might work directly
                                    AxumMessage::Close(Some(CloseFrame {
                                        code,
                                        reason: reason_str.into(),
                                    }))
                                }
                                None => AxumMessage::Close(None),
                            }
                        }
                        TungsteniteMessage::Frame(_) => continue, // 忽略原始帧消息
                    };

                    if let Err(e) = client_sender.send(converted_msg).await {
                        tracing::error!("Error forwarding message to client: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    tracing::error!("Error receiving message from server: {}", e);
                    break;
                }
            }
        }
    };

    // 同时处理两个方向的消息转发
    tokio::select! {
        _ = client_to_server => tracing::info!("Client to server connection closed"),
        _ = server_to_client => tracing::info!("Server to client connection closed"),
    }
}
