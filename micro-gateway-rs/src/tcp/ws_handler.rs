use axum::body::Body;
use axum::extract::WebSocketUpgrade;
use axum::extract::ws::{Message as AxumMessage, WebSocket};
use axum::response::{IntoResponse, Response};
use futures_util::{SinkExt, StreamExt};
use hyper::{HeaderMap, Method, StatusCode, Uri};
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::Message as TungsteniteMessage;
use url::Url;

use crate::client::http_client::AuthRouteResp;
use crate::exception::error::ClientError;
use crate::utils::http_util::{self};

pub async fn ws_route_handler(
    ws: WebSocketUpgrade,
    uri: Uri,
) -> Result<Response<Body>, StatusCode> {
    // Extract authentication token
    let token = extract_token(&uri);

    // Get authentication URL
    let auth_url = http_util::get_auth_url().map_err(|e| {
        ClientError::Status(
            StatusCode::INTERNAL_SERVER_ERROR.as_u16(),
            format!("Failed to get auth url: {}", e),
        )
    })?;

    // Prepare route request
    let req_body = http_util::prepare_route_request(&Method::GET, &HeaderMap::new(), &uri);

    // Forward to route service
    let route_resp = http_util::find_route(auth_url, req_body, &token)
        .await
        .map_err(|e| {
            ClientError::Status(
                StatusCode::INTERNAL_SERVER_ERROR.as_u16(),
                format!("Failed to find route: {}", e),
            )
        })?;

    // Parse url
    let new_url = parse_url(route_resp, &uri).map_err(|e| {
        ClientError::Status(
            StatusCode::INTERNAL_SERVER_ERROR.as_u16(),
            format!("Failed to parse url: {}", e),
        )
    })?;

    // 将 WebSocket 升级响应转换为 Response<Body>
    let response = ws
        .on_upgrade(|socket| async move {
            handle_websocket_request(socket, new_url).await;
        })
        .into_response();

    let (parts, _) = response.into_parts();
    Ok(Response::from_parts(parts, Body::empty()))
}

fn parse_url(route_resp: AuthRouteResp, uri: &Uri) -> Result<Url, ClientError> {
    let path_and_query = uri
        .path_and_query()
        .ok_or_else(|| ClientError::Request("Invalid URI".to_string()))?
        .to_string();

    let uri = format!(
        "{}://{}:{}{}",
        "ws",
        route_resp.service_host(),
        route_resp.service_port(),
        path_and_query
    );

    url::Url::parse(&uri).map_err(|e| ClientError::Request(e.to_string()))
}

fn extract_token(uri: &Uri) -> String {
    uri.query()
        .and_then(|q| {
            url::form_urlencoded::parse(q.as_bytes())
                .find(|(key, _)| key == "token")
                .map(|(_, value)| value.to_string())
        })
        .unwrap_or("".to_string())
}

async fn handle_websocket_request(ws: WebSocket, target_url: Url) {
    // 连接到微服务
    let (server_ws, _) = match connect_async(target_url.as_str()).await {
        Ok(conn) => conn,
        Err(e) => {
            log::error!("Failed to connect to websocket service: {}", e);
            return;
        }
    };

    // 使用 futures_util 的 split 方法
    let (mut client_sender, mut client_receiver) = futures_util::stream::StreamExt::split(ws);
    let (mut server_sender, mut server_receiver) =
        futures_util::stream::StreamExt::split(server_ws);

    // 从客户端到服务端的消息转发
    let client_to_server = async {
        while let Some(msg) = client_receiver.next().await {
            match msg {
                Ok(msg) => {
                    // 将 axum WebSocket 消息转换为 tungstenite 消息
                    let converted_msg = match msg {
                        AxumMessage::Text(text) => TungsteniteMessage::text(text.as_str()),
                        AxumMessage::Binary(data) => TungsteniteMessage::binary(data.to_vec()),
                        AxumMessage::Ping(data) => TungsteniteMessage::Ping(data.clone().into()),
                        AxumMessage::Pong(data) => TungsteniteMessage::Pong(data.clone().into()),
                        AxumMessage::Close(Some(frame)) => {
                            // Convert from u16 to CloseCode
                            let code = tokio_tungstenite::tungstenite::protocol::frame::coding::CloseCode::from(frame.code);

                            // If there's a way to create a CloseFrame from a reason string
                            let reason_string = frame.reason.as_str().to_string();

                            // Try using a builder pattern or other constructor
                            // Without seeing the full API, we can attempt a few approaches:

                            // Option 1: If there's a with_reason or similar method
                            let close_frame =
                                tokio_tungstenite::tungstenite::protocol::CloseFrame {
                                    code,
                                    reason: reason_string.into(), // Try with bytes directly
                                };

                            tokio_tungstenite::tungstenite::Message::Close(Some(close_frame))
                        }
                        AxumMessage::Close(None) => TungsteniteMessage::Close(None),
                    };

                    if let Err(e) = server_sender.send(converted_msg).await {
                        log::error!("Error forwarding message to server: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    log::error!("Error receiving message from client: {}", e);
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
                            AxumMessage::Text(axum::extract::ws::Utf8Bytes::from(text.as_str()))
                        }
                        TungsteniteMessage::Binary(data) => {
                            // 从 Vec<u8> 转为 axum 的 Bytes
                            AxumMessage::Binary(axum::body::Bytes::from(data))
                        }
                        TungsteniteMessage::Ping(data) => {
                            AxumMessage::Ping(axum::body::Bytes::from(data))
                        }
                        TungsteniteMessage::Pong(data) => {
                            AxumMessage::Pong(axum::body::Bytes::from(data))
                        }
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
                                    AxumMessage::Close(Some(axum::extract::ws::CloseFrame {
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
                        log::error!("Error forwarding message to client: {}", e);
                        break;
                    }
                }
                Err(e) => {
                    log::error!("Error receiving message from server: {}", e);
                    break;
                }
            }
        }
    };

    // 同时处理两个方向的消息转发
    tokio::select! {
        _ = client_to_server => log::info!("Client to server connection closed"),
        _ = server_to_client => log::info!("Server to client connection closed"),
    }
}
